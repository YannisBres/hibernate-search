/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.util.impl.integrationtest.common.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.CompletableFuture;

import org.hibernate.search.engine.backend.work.execution.DocumentCommitStrategy;
import org.hibernate.search.engine.backend.work.execution.DocumentRefreshStrategy;
import org.hibernate.search.util.common.impl.Futures;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.index.StubDocumentWork;

import org.junit.Rule;
import org.junit.Test;

import org.assertj.core.api.AbstractThrowableAssert;

public class BackendMockBackendIndexingWorkExpectationsTest {

	private static final String INDEX_NAME = "indexName";

	@Rule
	public BackendMock backendMock = new BackendMock();

	@Test
	public void syncDefaultAddWorkType() {
		backendMock.indexingWorkExpectations( BackendIndexingWorkExpectations.sync() );
		backendMock.expectWorks( INDEX_NAME ).executeFollowingWorks().add( b -> { } );
		assertThatCode( () -> backendMock.backendBehavior().executeDocumentWork(
				INDEX_NAME,
				StubDocumentWork.builder( StubDocumentWork.Type.ADD )
						.commit( DocumentCommitStrategy.FORCE )
						.refresh( DocumentRefreshStrategy.NONE )
						.build()
		) )
				.doesNotThrowAnyException();

		backendMock.expectWorks( INDEX_NAME ).executeFollowingWorks().add( b -> { } );
		assertThatCode( () -> backendMock.backendBehavior().executeDocumentWork(
				INDEX_NAME,
				StubDocumentWork.builder( StubDocumentWork.Type.ADD_OR_UPDATE )
						.commit( DocumentCommitStrategy.FORCE )
						.refresh( DocumentRefreshStrategy.NONE )
						.build()
		) )
				.isInstanceOf( AssertionError.class );
		backendMock.resetExpectations();
	}

	@Test
	public void asyncDefaultAddWorkType() {
		backendMock.indexingWorkExpectations( BackendIndexingWorkExpectations.async( ".*" ) );
		backendMock.expectWorks( INDEX_NAME ).executeFollowingWorks().add( b -> { } );
		assertThatCode( () -> backendMock.backendBehavior().executeDocumentWork(
				INDEX_NAME,
				StubDocumentWork.builder( StubDocumentWork.Type.ADD )
						.commit( DocumentCommitStrategy.FORCE )
						.refresh( DocumentRefreshStrategy.NONE )
						.build()
		) )
				.doesNotThrowAnyException();

		backendMock.expectWorks( INDEX_NAME ).executeFollowingWorks().add( b -> { } );
		assertThatCode( () -> backendMock.backendBehavior().executeDocumentWork(
				INDEX_NAME,
				StubDocumentWork.builder( StubDocumentWork.Type.ADD_OR_UPDATE )
						.commit( DocumentCommitStrategy.FORCE )
						.refresh( DocumentRefreshStrategy.NONE )
						.build()
		) )
				.isInstanceOf( AssertionError.class );
		backendMock.resetExpectations();
	}

	@Test
	public void asyncCustomAddWorkType() {
		// We tell the backend that all "add" expectations should instead be interpreted as "addOrUpdate",
		// because the mapper is configured to always issue ADD_OR_UPDATE operations.
		backendMock.indexingWorkExpectations( BackendIndexingWorkExpectations.async( ".*", StubDocumentWork.Type.ADD_OR_UPDATE ) );
		// We expect an "add" operation...
		backendMock.expectWorks( INDEX_NAME ).executeFollowingWorks().add( b -> { } );
		// We trigger an "addOrUpdate" operation...
		assertThatCode( () -> backendMock.backendBehavior().executeDocumentWork(
				INDEX_NAME,
				StubDocumentWork.builder( StubDocumentWork.Type.ADD_OR_UPDATE )
						.commit( DocumentCommitStrategy.FORCE )
						.refresh( DocumentRefreshStrategy.NONE )
						.build()
		) )
				// And they should match anyway.
				.doesNotThrowAnyException();

		// We expect an "add" operation...
		backendMock.expectWorks( INDEX_NAME ).executeFollowingWorks().add( b -> { } );
		// We trigger a "delete" operation...
		assertThatCode( () -> backendMock.backendBehavior().executeDocumentWork(
				INDEX_NAME,
				StubDocumentWork.builder( StubDocumentWork.Type.DELETE )
						.commit( DocumentCommitStrategy.FORCE )
						.refresh( DocumentRefreshStrategy.NONE )
						.build()
		) )
				// And they won't match.
				.isInstanceOf( AssertionError.class );
		backendMock.resetExpectations();
	}

	@Test
	public void workSubmittedFromExpectedThread() {
		backendMock.indexingWorkExpectations( BackendIndexingWorkExpectations.async( "matching.*" ) );
		backendMock.expectWorks( INDEX_NAME ).executeFollowingWorks().add( b -> { } );
		assertThatThrownByCodeRunningInThreadWithName( "matchingFoo",
				() -> {
					backendMock.backendBehavior().executeDocumentWork( INDEX_NAME,
							StubDocumentWork.builder( StubDocumentWork.Type.ADD )
									.commit( DocumentCommitStrategy.FORCE )
									.refresh( DocumentRefreshStrategy.NONE )
									.build() );
				} )
				.doesNotThrowAnyException();
	}

	@Test
	public void workSubmittedFromUnexpectedThread() {
		backendMock.indexingWorkExpectations( BackendIndexingWorkExpectations.async( "matching.*" ) );
		backendMock.expectWorks( INDEX_NAME ).executeFollowingWorks().add( b -> { } );
		assertThatThrownByCodeRunningInThreadWithName( "wrongName",
				() -> {
					backendMock.backendBehavior().executeDocumentWork( INDEX_NAME,
							StubDocumentWork.builder( StubDocumentWork.Type.ADD )
									.commit( DocumentCommitStrategy.FORCE )
									.refresh( DocumentRefreshStrategy.NONE )
									.build() );
				} )
				.isInstanceOf( AssertionError.class )
				.hasMessageContainingAll( "Name of current thread when executing work",
						"StubDocumentWork[type=ADD,",
						"Expecting", "wrongName", "to match pattern", "matching.*" );

		// Just tell backendMock to not fail this test:
		// we just had the failure we were looking for, so we're fine.
		backendMock.resetExpectations();
	}

	private AbstractThrowableAssert<?, ? extends Throwable> assertThatThrownByCodeRunningInThreadWithName(
			String threadName, Runnable runnable) {
		CompletableFuture<?> future = new CompletableFuture<>();
		Thread thread = new Thread(
				() -> {
					try {
						runnable.run();
						future.complete( null );
					}
					catch (Throwable t) {
						future.completeExceptionally( t );
					}
				},
				threadName
		);
		thread.start();
		await().untilAsserted( () -> assertThat( future ).isDone() );
		return assertThat( Futures.getThrowableNow( future ) );
	}

}