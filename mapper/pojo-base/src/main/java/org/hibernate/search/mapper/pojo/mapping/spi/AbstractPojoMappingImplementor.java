/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.mapping.spi;

import java.util.concurrent.CompletableFuture;

import org.hibernate.search.engine.backend.types.converter.runtime.ToDocumentValueConvertContext;
import org.hibernate.search.engine.backend.types.converter.runtime.spi.ToDocumentValueConvertContextImpl;
import org.hibernate.search.engine.backend.work.execution.DocumentCommitStrategy;
import org.hibernate.search.engine.backend.work.execution.DocumentRefreshStrategy;
import org.hibernate.search.engine.mapper.mapping.spi.MappingImplementor;
import org.hibernate.search.engine.mapper.mapping.spi.MappingPreStopContext;
import org.hibernate.search.engine.mapper.mapping.spi.MappingStartContext;
import org.hibernate.search.engine.search.projection.definition.spi.ProjectionRegistry;
import org.hibernate.search.mapper.pojo.bridge.runtime.IdentifierBridgeToDocumentIdentifierContext;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;
import org.hibernate.search.mapper.pojo.bridge.runtime.impl.IdentifierBridgeToDocumentIdentifierContextImpl;
import org.hibernate.search.mapper.pojo.bridge.runtime.impl.ValueBridgeToIndexedValueContextImpl;
import org.hibernate.search.mapper.pojo.scope.spi.PojoScopeMappingContext;
import org.hibernate.search.mapper.pojo.session.spi.PojoSearchSessionMappingContext;
import org.hibernate.search.mapper.pojo.work.spi.PojoIndexer;
import org.hibernate.search.mapper.pojo.work.spi.PojoIndexingPlan;
import org.hibernate.search.mapper.pojo.work.spi.PojoIndexingQueueEventProcessingPlan;
import org.hibernate.search.mapper.pojo.work.spi.PojoIndexingQueueEventSendingPlan;
import org.hibernate.search.mapper.pojo.work.spi.PojoWorkSessionContext;
import org.hibernate.search.util.common.impl.Closer;

public abstract class AbstractPojoMappingImplementor<M>
		implements MappingImplementor<M>, PojoScopeMappingContext, PojoSearchSessionMappingContext {

	private final PojoMappingDelegate delegate;

	private boolean stopped = false;

	private final ToDocumentValueConvertContext toDocumentValueConvertContext;
	private final IdentifierBridgeToDocumentIdentifierContext toDocumentIdentifierContext;
	private final ValueBridgeToIndexedValueContext toIndexedValueContext;

	public AbstractPojoMappingImplementor(PojoMappingDelegate delegate) {
		this.delegate = delegate;
		this.toDocumentValueConvertContext = new ToDocumentValueConvertContextImpl( this );
		this.toDocumentIdentifierContext = new IdentifierBridgeToDocumentIdentifierContextImpl( this );
		this.toIndexedValueContext = new ValueBridgeToIndexedValueContextImpl( this );
	}

	@Override
	public CompletableFuture<?> start(MappingStartContext context) {
		// Nothing to do
		return CompletableFuture.completedFuture( null );
	}

	@Override
	public CompletableFuture<?> preStop(MappingPreStopContext context) {
		// Nothing to do
		return CompletableFuture.completedFuture( null );
	}

	@Override
	public void stop() {
		if ( !stopped ) {
			// Make sure to avoid infinite recursion when one of the delegates calls this.stop()
			stopped = true;
			try ( Closer<RuntimeException> closer = new Closer<>() ) {
				closer.push( PojoMappingDelegate::close, delegate );
				closer.push( AbstractPojoMappingImplementor::doStop, this );
			}
		}
	}

	@Override
	public final ToDocumentValueConvertContext toDocumentValueConvertContext() {
		return toDocumentValueConvertContext;
	}

	@Override
	public ProjectionRegistry projectionRegistry() {
		return delegate.projectionRegistry();
	}

	@Override
	public final IdentifierBridgeToDocumentIdentifierContext identifierBridgeToDocumentIdentifierContext() {
		return toDocumentIdentifierContext;
	}

	@Override
	public ValueBridgeToIndexedValueContext valueBridgeToIndexedValueContext() {
		return toIndexedValueContext;
	}

	@Override
	public PojoIndexingPlan createIndexingPlan(PojoWorkSessionContext context,
			DocumentCommitStrategy commitStrategy, DocumentRefreshStrategy refreshStrategy) {
		return delegate.createIndexingPlan( context, commitStrategy, refreshStrategy );
	}

	@Override
	public PojoIndexingPlan createIndexingPlan(PojoWorkSessionContext context, PojoIndexingQueueEventSendingPlan sendingPlan) {
		return delegate.createIndexingPlan( context, sendingPlan );
	}

	@Override
	public PojoIndexer createIndexer(PojoWorkSessionContext context) {
		return delegate.createIndexer( context );
	}

	@Override
	public PojoIndexingQueueEventProcessingPlan createIndexingQueueEventProcessingPlan(PojoWorkSessionContext context,
			DocumentCommitStrategy commitStrategy, DocumentRefreshStrategy refreshStrategy,
			PojoIndexingQueueEventSendingPlan sendingPlan) {
		return delegate.createEventProcessingPlan( context, commitStrategy, refreshStrategy, sendingPlan );
	}

	protected final PojoMappingDelegate delegate() {
		return delegate;
	}

	protected void doStop() {
	}

}
