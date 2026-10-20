package com.udea.labdrools.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Genera una explicación amigable (en español) de la decisión tomada por las
 * reglas de negocio, usando el modelo de lenguaje de Spring AI / OpenAI.
 *
 * El servicio es "best effort": si el modelo no está disponible (por ejemplo
 * falta la API key o no hay conectividad), se devuelve un mensaje de respaldo
 * en lugar de propagar la excepción.
 */
@Service
public class AIExplanationService {

    private static final String PROMPT_TEMPLATE = """
            Eres el asistente virtual de una aerolínea.
            Explica en español, en máximo 3 frases y con un lenguaje sencillo para el pasajero,
            la siguiente decisión tomada por el sistema de reglas de la aerolínea.
            Si la decisión es un rechazo o una restricción, explica cortésmente el motivo:
            %s
            """;

    private final ChatClient chatClient;

    public AIExplanationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * @param decision texto con la(s) mensaje(s) de la evaluación de Drools
     * @return explicación generada por la IA, o un mensaje de respaldo si falla
     */
    public String explain(String decision) {
        if (decision == null || decision.isBlank()) {
            return "No hay una decisión para explicar.";
        }
        try {
            return chatClient.prompt()
                    .user(String.format(PROMPT_TEMPLATE, decision))
                    .call()
                    .content();
        } catch (Exception e) {
            return "No fue posible generar la explicación con IA: " + e.getMessage();
        }
    }
}
