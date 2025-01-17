/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.search.projection.impl;

import org.hibernate.search.backend.lucene.search.common.impl.LuceneSearchIndexScope;
import org.hibernate.search.engine.backend.common.DocumentReference;
import org.hibernate.search.engine.search.common.spi.SearchIndexIdentifierContext;
import org.hibernate.search.engine.search.projection.SearchProjection;
import org.hibernate.search.engine.search.projection.spi.CompositeProjectionBuilder;
import org.hibernate.search.engine.search.projection.spi.SearchProjectionBuilderFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Explanation;

public class LuceneSearchProjectionBuilderFactory implements SearchProjectionBuilderFactory {

	private final LuceneSearchIndexScope<?> scope;

	public LuceneSearchProjectionBuilderFactory(LuceneSearchIndexScope<?> scope) {
		this.scope = scope;
	}

	@Override
	public SearchProjection<DocumentReference> documentReference() {
		return new LuceneDocumentReferenceProjection( scope );
	}

	@Override
	public <E> SearchProjection<E> entity() {
		return new LuceneEntityProjection<>( scope );
	}

	@Override
	public <R> SearchProjection<R> entityReference() {
		return new LuceneEntityReferenceProjection<>( scope );
	}

	@Override
	public <I> SearchProjection<I> id(Class<I> identifierType) {
		SearchIndexIdentifierContext identifier = scope.identifier();
		return new LuceneIdProjection<>( scope,
				identifier.projectionConverter().withConvertedType( identifierType, identifier ) );
	}

	@Override
	public SearchProjection<Float> score() {
		return new LuceneScoreProjection( scope );
	}

	@Override
	public CompositeProjectionBuilder composite() {
		return new LuceneCompositeProjection.Builder( scope );
	}

	@Override
	public <T> SearchProjection<T> constant(T value) {
		return new LuceneConstantProjection<>( scope, value );
	}

	public SearchProjection<Document> document() {
		return new LuceneDocumentProjection( scope );
	}

	public SearchProjection<Explanation> explanation() {
		return new LuceneExplanationProjection( scope );
	}

}
