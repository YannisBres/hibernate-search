/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.standalone.massindexing.impl;

import org.hibernate.search.engine.backend.session.spi.DetachedBackendSessionContext;
import org.hibernate.search.mapper.pojo.massindexing.spi.PojoMassIndexingMappingContext;
import org.hibernate.search.mapper.pojo.model.spi.PojoRuntimeIntrospector;

public interface StandalonePojoMassIndexingMappingContext extends PojoMassIndexingMappingContext {

	StandalonePojoMassIndexingSessionContext createSession(DetachedBackendSessionContext sessionContext);

	PojoRuntimeIntrospector runtimeIntrospector();

}
