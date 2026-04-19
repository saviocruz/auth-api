package br.local.auth.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import br.local.auth.dto.PerfilDTO;

import java.io.IOException;

/**
 * Custom deserializer for PerfilDTO that can handle both full objects and numeric IDs.
 * When a numeric ID is provided, it creates a PerfilDTO with just the ID set.
 */
public class PerfilDTODeserializer extends JsonDeserializer<PerfilDTO> {

    @Override
    public PerfilDTO deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        
        JsonNode node = p.getCodec().readTree(p);
        
        // If it's a numeric value, treat it as an ID
        if (node.isNumber()) {
            PerfilDTO perfilDTO = new PerfilDTO();
            perfilDTO.setId(node.asLong());
            return perfilDTO;
        }
        
        // If it's an object, let the default deserializer handle it
        if (node.isObject()) {
            // Use the default ObjectMapper to deserialize the object
            return p.getCodec().treeToValue(node, PerfilDTO.class);
        }
        
        // For other cases, return null or throw an exception
        return null;
    }
}