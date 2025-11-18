package br.univille.chat.interfaces.rest;

import br.univille.chat.application.ConversationRepository;
import br.univille.chat.application.MessageRepository;
import br.univille.chat.domain.Conversation;
import br.univille.chat.domain.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    public record MessageRequest(String content) {}
    public record ConversationRequest(UUID recipientId) {}

    // Helper para extrair o ID do usuário do cabeçalho
    private UUID getAuthenticatedUserId(String userIdHeader) {
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user ID in header: X-User-ID.");
        }
    }

    /**
     * Inicia uma nova conversa ou retorna o ID da conversa existente entre dois usuários.
     * Mapeado para /chat/messages/conversation
     */
    @PostMapping("/conversation")
    public ResponseEntity<UUID> startConversation(@RequestHeader("X-User-ID") String userIdHeader,
                                                    @RequestBody ConversationRequest request) {
        UUID user1 = getAuthenticatedUserId(userIdHeader);
        UUID user2 = request.recipientId();

        if (user1.equals(user2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot start a conversation with yourself.");
        }

        Conversation conversation = conversationRepository.findConversationByParticipants(user1, user2)
                .orElseGet(() -> {
                    Conversation newConversation = new Conversation(user1, user2);
                    return conversationRepository.save(newConversation);
                });

        return ResponseEntity.ok(conversation.getId());
    }

    /**
     * Envia uma nova mensagem para uma conversa existente.
     * Mapeado para /chat/messages/{conversationId}
     */
    @PostMapping("/{conversationId}")
    public ResponseEntity<Message> sendMessage(@PathVariable UUID conversationId,
                                                @RequestHeader("X-User-ID") String userIdHeader,
                                                @RequestBody MessageRequest request) {
        UUID senderId = getAuthenticatedUserId(userIdHeader);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found."));

        if (!conversation.getParticipants().contains(senderId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a participant of this conversation.");
        }

        Message message = new Message(conversationId, senderId, request.content());
        Message saved = messageRepository.save(message);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Recupera todas as mensagens de uma conversa.
     * Mapeado para /chat/messages/{conversationId}
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable UUID conversationId,
                                                     @RequestHeader("X-User-ID") String userIdHeader) {
        UUID authenticatedUserId = getAuthenticatedUserId(userIdHeader);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found."));

        if (!conversation.getParticipants().contains(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a participant of this conversation.");
        }

        List<Message> messages = messageRepository.findAllByConversationIdOrderBySentAtAsc(conversationId);
        return ResponseEntity.ok(messages);
    }
}