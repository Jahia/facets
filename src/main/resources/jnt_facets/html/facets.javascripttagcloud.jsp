<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="facets.css"/>
<template:addResources type="css" resources="jqcloud.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jqcloud.js"/>
<c:set var="boundComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<template:addCacheDependency node="${boundComponent}"/>
<jcr:nodeProperty var="facetListNodeType" node="${currentNode}" name="j:type" />
<c:if test="${not empty boundComponent}">
    <c:set var="facetParamVarName" value="N-${boundComponent.name}"/>
    <c:set var="facetTargetTypeName" value="N-type-${boundComponent.name}"/>
    <c:set var="activeFacetMapVarName" value="afm-${boundComponent.name}"/>
    <c:if test="${not empty param[facetParamVarName] and empty activeFacetsVars[facetParamVarName]}">
        <c:if test="${activeFacetsVars == null}">
            <jsp:useBean id="activeFacetsVars" class="java.util.HashMap" scope="request"/>
        </c:if>
        <c:set target="${activeFacetsVars}" property="${facetParamVarName}"
               value="${functions:decodeUrlParam(param[facetParamVarName])}"/>
        <c:set target="${activeFacetsVars}" property="${activeFacetMapVarName}"
               value="${facet:getAppliedFacetFilters(activeFacetsVars[facetParamVarName])}"/>
    </c:if>

    <jsp:useBean id="facetLabels" class="java.util.HashMap" scope="request"/>
    <jsp:useBean id="facetValueLabels" class="java.util.HashMap" scope="request"/>
    <jsp:useBean id="facetValueFormats" class="java.util.HashMap" scope="request"/>

    <template:option node="${boundComponent}" nodetype="${boundComponent.primaryNodeTypeName},jmix:list" view="hidden.load">
        <template:param name="queryLoadAllUnsorted" value="true"/>
    </template:option>

    <facet:setupQueryAndMetadata var="listQuery" boundComponent="${boundComponent}" existingQuery="${moduleMap.listQuery}"
                                 activeFacets="${not empty activeFacetsVars ? activeFacetsVars[activeFacetMapVarName] : null}"/>
    <jcr:jqom var="result" qomBeanName="listQuery" scope="request"/>
    <c:if test="${!empty activeFacetsVars and !empty activeFacetsVars[activeFacetMapVarName]}">
        <div class="facets">
            <%@include file="activeFacets.jspf" %>
        </div>
    </c:if>
    <c:if test="${(result.facetFields[0].valueCount gt 0)}">
        <h3><c:if
                test="${not empty currentNode.properties['jcr:title'] && not empty currentNode.properties['jcr:title'].string}"
                var="titleProvided">${fn:escapeXml(currentNode.properties['jcr:title'].string)}</c:if><c:if
                test="${not titleProvided}"><fmt:message key="tags"/></c:if></h3>
        <jsp:useBean id="tagCloud" class="java.util.HashMap"/>
        <c:forEach items="${result.facetFields}" var="tags">
            <c:forEach items="${tags.values}" var="tag">
                <c:set var="totalUsages" value="${totalUsages + tag.count}"/>
                <c:set target="${tagCloud}" property="${tag.name}" value="${tag.count}"/>
            </c:forEach>
        </c:forEach>

        <c:if test="${not empty tagCloud}">
            <c:forEach items="${result.facetFields}" var="currentFacet">
                <template:addResources type="inlinejavascript" key="wordListForCloud">
                    <script type="text/javascript">
                        var word_list = new Array(<c:forEach items="${currentFacet.values}" var="facetValue" varStatus="status">
                                <c:if test="${not facet:isFacetValueApplied(facetValue, not empty activeFacetsVars ? activeFacetsVars[activeFacetMapVarName] : null)}">
                                <c:url var="facetUrl" value="${url.mainResource}">
                                <c:param name="${facetTargetTypeName}" value="${functions:encodeUrlParam(facetListNodeType)}" />
                                <c:param name="${facetParamVarName}"
                               value="${functions:encodeUrlParam(facet:getFacetDrillDownUrl(facetValue, not empty activeFacetsVars ? activeFacetsVars[facetParamVarName] : null))}"/>
                                </c:url>
                                <facet:facetValueLabel currentFacetFieldName="${currentFacet.name}"
                               facetValueCount="${facetValue}"
                               facetValueLabels="${facetValueLabels}"
                               facetValueFormats="${facetValueFormats}" display="false"/>
                                {text: "${facetValueLabel}", weight:${functions:round(10 * tagCloud[facetValue.name] / totalUsages)}, url: "${facetUrl}"}
                                <c:if test="${not status.last}">, </c:if>
                                </c:if>
                                </c:forEach>);
                        $(document).ready(function () {
                            $("#wordcloud").jQCloud(word_list);
                        });
                    </script>
                </template:addResources>
            </c:forEach>
            <div id="wordcloud" style="width: 280px; height: 200px;"></div>
        </c:if>
    </c:if>
</c:if>
<c:if test="${renderContext.editMode}">
    <fmt:message key="facets.facetsSet"/> :
    <c:forEach items="${jcr:getNodes(currentNode, 'jnt:facet')}" var="facet">
        <template:module node="${facet}"/>
    </c:forEach>
    <template:module path="*"/>
    <fmt:message key="${fn:replace(currentNode.primaryNodeTypeName,':','_')}"/>
</c:if>
