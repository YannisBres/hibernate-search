/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.orchestration.impl;

import java.util.concurrent.CompletableFuture;

import org.hibernate.search.backend.elasticsearch.work.impl.ElasticsearchWork;
import org.hibernate.search.engine.backend.orchestration.spi.BatchedWork;
import org.hibernate.search.util.common.impl.Futures;

class ElasticsearchBatchedWork<T> implements BatchedWork<ElasticsearchWorkProcessor> {
	private final ElasticsearchWork<T> work;
	private final CompletableFuture<T> future;

	ElasticsearchBatchedWork(ElasticsearchWork<T> work, CompletableFuture<T> future) {
		this.work = work;
		this.future = future;
	}

	@Override
	public void submitTo(ElasticsearchWorkProcessor delegate) {
		delegate.submit( work ).whenComplete( Futures.copyHandler( future ) );
	}

	@Override
	public void markAsFailed(Throwable t) {
		future.completeExceptionally( t );
	}
}