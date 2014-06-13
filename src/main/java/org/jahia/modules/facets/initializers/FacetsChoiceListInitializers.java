/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.modules.facets.initializers;

import org.slf4j.Logger;
import org.drools.core.util.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ComponentLinkerChoiceListInitializer;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.PropertyDefinition;

import java.util.*;

/**
 * User: david
 * Date: Apr 19, 2010
 * Time: 3:03:45 PM
 */
public class FacetsChoiceListInitializers implements ModuleChoiceListInitializer {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(FacetsChoiceListInitializers.class);
    private String key;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        final Set<ChoiceListValue> propertyNames = new HashSet<ChoiceListValue>();
        try {
            for (ExtendedPropertyDefinition propertyDef : getPropertyDefinitions(
                    param, context)) {
                ExtendedNodeType nt = propertyDef.getDeclaringNodeType();
                String displayName = propertyDef.getLabel(locale, nt);
                displayName += nt.isMixin() ? "" : " (" + nt.getLabel(locale)
                        + ")";
                String value = nt.getName() + ";" + propertyDef.getName();
                propertyNames.add(new ChoiceListValue(displayName,
                        new HashMap<String, Object>(), new ValueImpl(value,
                                PropertyType.STRING, false)));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        List<ChoiceListValue> listValues = new ArrayList<ChoiceListValue>(propertyNames);
        Collections.sort(listValues);
        return listValues;
    }
    
    private List<ExtendedPropertyDefinition> getPropertyDefinitions(String param,
            Map<String, Object> context)
            throws RepositoryException {
        List<ExtendedPropertyDefinition> propDefs = null;
        if (StringUtils.isEmpty(param)) {
            JCRNodeWrapper parentNode = (JCRNodeWrapper) context
                    .get("contextParent");
            if (parentNode == null) {
                JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) context
                        .get("contextNode");
                if (nodeWrapper != null) {
                    parentNode = nodeWrapper.getParent();
                }
            }

            if (parentNode != null
                    && parentNode.hasProperty("j:bindedComponent")) {
                JCRNodeWrapper boundNode = (JCRNodeWrapper) parentNode
                        .getProperty("j:bindedComponent").getNode();
                if (boundNode.hasProperty("j:allowedTypes")) {
                    final Value[] values1 = boundNode.getProperty(
                            "j:allowedTypes").getValues();
                    ExtendedPropertyDefinition[] propertyDefs = ComponentLinkerChoiceListInitializer
                            .getCommonChildNodeDefinitions(values1, true, true,
                                    new LinkedHashSet<String>());
                    propDefs = Arrays.asList(propertyDefs);
                }
            }
        }
        if (propDefs == null || propDefs.isEmpty()) {
            propDefs = new ArrayList<ExtendedPropertyDefinition>();
            boolean hierarchical = param.contains("hierarchical");
            int requiredType = param.contains("date") ? PropertyType.DATE
                    : PropertyType.UNDEFINED;

            NodeTypeIterator ntr = NodeTypeRegistry.getInstance()
                    .getAllNodeTypes();
            while (ntr.hasNext()) {
                ExtendedNodeType nt = (ExtendedNodeType) ntr.nextNodeType();
                for (PropertyDefinition def : nt.getPropertyDefinitions()) {
                    ExtendedPropertyDefinition ep = (ExtendedPropertyDefinition) def;
                    if (ep.isFacetable()
                            && (!hierarchical || ep.isHierarchical())
                            && (requiredType == PropertyType.UNDEFINED || ep
                                    .getRequiredType() == requiredType)) {
                        propDefs.add(ep);
                    }
                }
            }
        }
        return propDefs;
    }
}
