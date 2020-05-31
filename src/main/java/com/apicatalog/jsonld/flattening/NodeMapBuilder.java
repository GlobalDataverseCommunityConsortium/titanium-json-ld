package com.apicatalog.jsonld.flattening;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import com.apicatalog.jsonld.api.JsonLdError;
import com.apicatalog.jsonld.grammar.CompactUri;
import com.apicatalog.jsonld.grammar.Keywords;
import com.apicatalog.jsonld.grammar.NodeObject;
import com.apicatalog.jsonld.json.JsonUtils;

public final class NodeMapBuilder {

    // required
    private JsonStructure element;
    private NodeMap nodeMap;
    private final BlankNodeIdGenerator idGenerator;
    
    // optional
    private String activeGraph;
    private String activeSubject;
    private String activeProperty;
    private Map list;   //TODO
    
    
    private NodeMapBuilder(final JsonStructure element, final NodeMap nodeMap, final BlankNodeIdGenerator idGenerator) {
        this.element = element;
        this.nodeMap = nodeMap;
        this.idGenerator = idGenerator;
        
        // default values
        this.activeGraph = Keywords.DEFAULT;
        this.activeSubject = null;
        this.activeProperty = null;
        this.list = null;
    }
    
    public static final NodeMapBuilder with(final JsonStructure element, final NodeMap nodeMap, final BlankNodeIdGenerator idGenerator) {
        return new NodeMapBuilder(element, nodeMap, idGenerator);
    }
    
    public NodeMapBuilder activeGraph(String activeGraph) {
        this.activeGraph = activeGraph;
        return this;
    }
    
    public NodeMapBuilder activeProperty(String activeProperty) {
        this.activeProperty = activeProperty;
        return this;
    }
    
    public NodeMapBuilder activeSubject(String activeSubject) {
        this.activeSubject = activeSubject;
        return this;
    }
    
    public NodeMapBuilder list(Map list) {
        this.list = list;
        return this;
    }
    
    public void build() throws JsonLdError {
        
        // 1.
        if (JsonUtils.isArray(element)) {
            
            // 1.1.
            for (JsonValue item : element.asJsonArray()) {
                
                JsonStructure itemValue;
                
                if (JsonUtils.isObject(item)) {
                    itemValue = item.asJsonObject();
                                    
                } else if (JsonUtils.isArray(item)) {
                    itemValue = item.asJsonArray();
                    
                } else {
                    throw new IllegalStateException();
                }
                
                NodeMapBuilder
                    .with(itemValue, nodeMap, idGenerator)
                    .activeGraph(activeGraph)
                    .activeProperty(activeProperty)
                    .activeSubject(activeSubject)
                    .list(list)
                    .build();

            }
            return;
        }
        
        // 2.        
        Map<String, JsonValue> elementObject = new LinkedHashMap<>(element.asJsonObject());
        
        // 3.
        if (elementObject.containsKey(Keywords.TYPE)) {
            
            // 3.1.
            for (JsonValue item : JsonUtils.toJsonArray(elementObject.get(Keywords.TYPE))) {
                
                if (JsonUtils.isString(item) && CompactUri.isBlankNode(((JsonString)item).getString())) {
                    //TODO
                }
            }
        }
        
        // 4.
        if (elementObject.containsKey(Keywords.VALUE)) {

            // 4.1.
            if (list == null) {
                
                // 4.1.1.
                if (nodeMap.doesNotContain(activeGraph, activeSubject, activeProperty)) {
                    nodeMap.set(activeGraph, activeSubject, activeProperty, Json.createArrayBuilder().add(JsonUtils.toJsonObject(elementObject)).build());
                    
                // 4.1.2.
                } else {

                    JsonArray activePropertyValue = nodeMap.get(activeGraph, activeSubject, activeProperty).asJsonArray();
                    
                    if (activePropertyValue.stream().noneMatch(e -> Objects.equals(e, element))) {
                        nodeMap.set(activeGraph, activeSubject, activeProperty, Json.createArrayBuilder(activePropertyValue).add(element).build());
                    }                    
                }

            // 4.2.
            } else {

                //TODO
                
            }
            
        // 5.
        } else if (elementObject.containsKey(Keywords.LIST)) {
        
            // 5.1.
            Map<String, JsonValue> result = new LinkedHashMap<>();
            result.put(Keywords.LIST, JsonValue.EMPTY_JSON_ARRAY);
            
            // 5.2.
            //TODO
            
            // 5.3.
            if (list == null) {
                //TODO
            // 5.4.
            } else {
                //TODO
            }
            
        // 6.
        } else if (NodeObject.isNodeObject(element)) {

            String id = null;
            
            // 6.1.
            if (elementObject.containsKey(Keywords.ID)) {
                
                id = ((JsonString)elementObject.get(Keywords.ID)).getString();
                
                if (CompactUri.isBlankNode(id)) {
                    id = idGenerator.createIdentifier(id);
                }
                
                //TODO
                elementObject.remove(Keywords.ID);

            // 6.2.
            } else {                
                id = idGenerator.createIdentifier();
            }
            
            // 6.3.
            if (nodeMap.doesNotContain(activeGraph, id)) {
                nodeMap.set(activeGraph, id, Keywords.ID, Json.createValue(id));
            }
            
            // 6.4.
//            Map<String, JsonValue> node = new LinkedHashMap<>(nodeMap.get());
            
            // 6.5.
            //TODO
            
            // 6.6.
            //TODO
            
            // 6.7.
            if (elementObject.containsKey(Keywords.TYPE)) {

                List<JsonValue> nodeType = null;
                
                JsonValue nodeTypeValue = nodeMap.get(activeGraph, id, Keywords.TYPE);
                
                if (JsonUtils.isArray(nodeTypeValue)) {
                    nodeType = new LinkedList<>(nodeTypeValue.asJsonArray());
                    
                } else if (JsonUtils.isNotNull(nodeTypeValue)) {
                    
                    nodeType = new LinkedList<>();
                    nodeType.add(nodeTypeValue);
                    
                } else {
                    nodeType = new LinkedList<>();
                }
                
                for (JsonValue item : JsonUtils.toJsonArray(elementObject.get(Keywords.TYPE))) {
                    nodeType.add(item);
                }

                nodeMap.set(activeGraph, id, Keywords.TYPE, JsonUtils.toJsonArray(nodeType));
                
                elementObject.remove(Keywords.TYPE);
            }

            // 6.8.
            if (elementObject.containsKey(Keywords.INDEX)) {
                //TODO
                elementObject.remove(Keywords.INDEX);
            }

            // 6.9.
            if (elementObject.containsKey(Keywords.REVERSE)) {
                //TODO
                elementObject.remove(Keywords.REVERSE);
            }

            // 6.10.
            if (elementObject.containsKey(Keywords.GRAPH)) {
                //TODO
                
                elementObject.remove(Keywords.GRAPH);
            }

            // 6.11.
            if (elementObject.containsKey(Keywords.INCLUDED)) {
                //TODO
                elementObject.remove(Keywords.INCLUDED);
            }

            // 6.12.
            for (String property : elementObject.keySet().stream().sorted().collect(Collectors.toList())) {
                
                JsonStructure value = (JsonStructure)elementObject.get(property);
                
                // 6.12.1.
                if (CompactUri.isBlankNode(property)) {
                    property = idGenerator.createIdentifier(property);
                }
                
                // 6.12.2.
                if (nodeMap.doesNotContain(activeGraph, id, property)) {
                    nodeMap.set(activeGraph, id, property, JsonValue.EMPTY_JSON_ARRAY);
                }

                // 6.12.3.
                NodeMapBuilder
                        .with(value, nodeMap, idGenerator)
                        .activeGraph(activeGraph)
                        .activeSubject(id)
                        .activeProperty(property)
                        .build();

            }
            
        }        
    }
    
}


