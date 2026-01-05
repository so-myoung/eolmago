package kr.eolmago.controller.view.chat;

import java.util.UUID;
import kr.eolmago.domain.entity.chat.ChatRoom;
import kr.eolmago.global.security.CustomUserDetails;
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
	public String chatRoom(@PathVariable Long roomId,
		@org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails me,
		Model model) {

		if (me == null || me.getId() == null || me.getId().isBlank()) {
			return "redirect:/login";
		}

		UUID userId = UUID.fromString(me.getId());

		ChatRoom room = chatRoomService.getRoomOrThrow(roomId);
		chatRoomService.validateParticipant(room, userId);

		model.addAttribute("roomId", roomId);
		model.addAttribute("userId", userId.toString());
		return "pages/chat/chat-room";
	}
}
