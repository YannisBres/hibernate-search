/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.util.impl.integrationtest.common.stub.backend.search.common.impl;

import java.util.List;
import java.util.Set;

import org.hibernate.search.engine.backend.mapping.spi.BackendMappingContext;
import org.hibernate.search.engine.backend.scope.spi.AbstractSearchIndexScope;
import org.hibernate.search.engine.backend.session.spi.BackendSessionContext;
import org.hibernate.search.engine.search.aggregation.spi.SearchAggregationBuilderFactory;
import org.hibernate.search.engine.search.loading.spi.SearchLoadingContextBuilder;
import org.hibernate.search.engine.search.predicate.spi.SearchPredicateBuilderFactory;
import org.hibernate.search.engine.search.projection.SearchProjection;
import org.hibernate.search.engine.search.projection.spi.SearchProjectionBuilderFactory;
import org.hibernate.search.engine.search.query.spi.SearchQueryBuilder;
import org.hibernate.search.engine.search.sort.spi.SearchSortBuilderFactory;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.document.model.impl.StubIndexModel;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.index.impl.StubBackend;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.search.aggregation.impl.StubSearchAggregationBuilderFactory;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.search.predicate.impl.StubSearchPredicateBuilderFactory;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.search.projection.impl.StubSearchProjection;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.search.projection.impl.StubSearchProjectionBuilderFactory;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.search.query.impl.StubSearchQueryBuilder;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.search.sort.impl.StubSearchSortBuilderFactory;

public class StubSearchIndexScope
		extends AbstractSearchIndexScope<
						StubSearchIndexScope,
						StubIndexModel,
						StubSearchIndexNodeContext,
						StubSearchIndexCompositeNodeContext
				> {
	private final StubBackend backend;
	private final StubSearchPredicateBuilderFactory predicateFactory;
	private final StubSearchSortBuilderFactory sortFactory;
	private final StubSearchProjectionBuilderFactory projectionFactory;
	private final StubSearchAggregationBuilderFactory aggregationFactory;

	public StubSearchIndexScope(BackendMappingContext mappingContext, StubBackend backend, Set<StubIndexModel> indexModels) {
		super( mappingContext, indexModels );
		this.backend = backend;
		this.predicateFactory = new StubSearchPredicateBuilderFactory();
		this.sortFactory = new StubSearchSortBuilderFactory();
		this.projectionFactory = new StubSearchProjectionBuilderFactory( this );
		this.aggregationFactory = new StubSearchAggregationBuilderFactory();
	}

	@Override
	protected StubSearchIndexScope self() {
		return this;
	}

	@Override
	public SearchPredicateBuilderFactory predicateBuilders() {
		return predicateFactory;
	}

	@Override
	public SearchProjectionBuilderFactory projectionBuilders() {
		return projectionFactory;
	}

	@Override
	public SearchSortBuilderFactory sortBuilders() {
		return sortFactory;
	}

	@Override
	public SearchAggregationBuilderFactory aggregationBuilders() {
		return aggregationFactory;
	}

	@Override
	public <P> SearchQueryBuilder<P> select(BackendSessionContext sessionContext,
			SearchLoadingContextBuilder<?, ?, ?> loadingContextBuilder, SearchProjection<P> projection) {
		return new StubSearchQueryBuilder<>( backend, this, sessionContext, loadingContextBuilder,
				(StubSearchProjection<P>) projection );
	}

	@Override
	protected StubSearchIndexCompositeNodeContext createMultiIndexSearchRootContext(
			List<StubSearchIndexCompositeNodeContext> rootForEachIndex) {
		return new StubMultiIndexSearchIndexCompositeNodeContext( this, null,
				rootForEachIndex );
	}

	@Override
	@SuppressWarnings("unchecked")
	protected StubSearchIndexNodeContext createMultiIndexSearchValueFieldContext(String absolutePath,
			List<StubSearchIndexNodeContext> fieldForEachIndex) {
		return new StubMultiIndexSearchIndexValueFieldContext<>( this, absolutePath,
				(List) fieldForEachIndex );
	}

	@Override
	@SuppressWarnings("unchecked")
	protected StubSearchIndexNodeContext createMultiIndexSearchObjectFieldContext(String absolutePath,
			List<StubSearchIndexNodeContext> fieldForEachIndex) {
		return new StubMultiIndexSearchIndexCompositeNodeContext( this, absolutePath,
				(List) fieldForEachIndex );
	}
}