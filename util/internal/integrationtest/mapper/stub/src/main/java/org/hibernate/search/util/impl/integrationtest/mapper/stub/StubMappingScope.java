/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.util.impl.integrationtest.mapper.stub;

import org.hibernate.search.engine.mapper.scope.spi.MappedIndexScope;
import org.hibernate.search.engine.backend.common.DocumentReference;
import org.hibernate.search.engine.search.query.dsl.SearchQuerySelectStep;

/**
 * A wrapper around {@link MappedIndexScope} providing some syntactic sugar,
 * such as methods that do not force to provide a session context.
 * <p>
 * This is a simpler version of {@link GenericStubMappingScope} that allows user to skip the generic parameters.
 */
public class StubMappingScope extends GenericStubMappingScope<DocumentReference, DocumentReference> {

	StubMappingScope(StubMapping mapping, MappedIndexScope<DocumentReference, DocumentReference> delegate) {
		super( mapping, delegate );
	}

	public SearchQuerySelectStep<?, DocumentReference, DocumentReference, StubLoadingOptionsStep, ?, ?> query() {
		return query( new StubSearchLoadingContext() );
	}

	public SearchQuerySelectStep<?, DocumentReference, DocumentReference, StubLoadingOptionsStep, ?, ?> query(
			StubSession sessionContext) {
		return query( sessionContext, new StubSearchLoadingContext() );
	}
}
