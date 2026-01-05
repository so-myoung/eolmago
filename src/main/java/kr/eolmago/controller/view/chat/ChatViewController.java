package kr.eolmago.controller.view.chat;

import java.security.Principal;
import java.util.UUID;

import kr.eolmago.domain.entity.chat.ChatRoom;
import kr.eolmago.service.chat.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chats")
public class ChatViewController {

	private final ChatRoomService chatRoomService;

	@GetMapping
	public String chatList() {
		return "pages/chat/chat-list";
	}

	@GetMapping("/rooms/{roomId}")
	public String chatRoom(@PathVariable Long roomId, Principal principal, Model model) {
		UUID userId = UUID.fromString(principal.getName());

		ChatRoom room = chatRoomService.getRoomOrThrow(roomId);
		chatRoomService.validateParticipant(room, userId);

		model.addAttribute("roomId", roomId);
		model.addAttribute("userId", userId.toString());
		return "pages/chat/chat-room";
	}
}
