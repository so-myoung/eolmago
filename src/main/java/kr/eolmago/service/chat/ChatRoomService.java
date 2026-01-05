package kr.eolmago.service.chat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import kr.eolmago.domain.entity.auction.Auction;
import kr.eolmago.domain.entity.chat.ChatRoom;
import kr.eolmago.domain.entity.user.User;
import kr.eolmago.dto.api.chat.response.ChatRoomSummaryResponse;
import kr.eolmago.repository.auction.AuctionRepository;
import kr.eolmago.repository.chat.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final AuctionRepository auctionRepository;

	@Transactional
	public Long createOrGetRoomForWinner(UUID auctionId) {
		Optional<ChatRoom> existing = chatRoomRepository.findByAuctionAuctionId(auctionId);
		if (existing.isPresent()) {
			return existing.get().getChatRoomId();
		}

		Auction auction = auctionRepository.findById(auctionId)
			.orElseThrow(() -> new IllegalArgumentException("auction not found"));

		User seller = auction.getSeller();
		User buyer = auction.getBuyer();
		if (buyer == null) {
			throw new IllegalStateException("buyer not decided yet");
		}

		ChatRoom newRoom = ChatRoom.create(auction, seller, buyer);

		try {
			ChatRoom saved = chatRoomRepository.save(newRoom);
			return saved.getChatRoomId();
		} catch (DataIntegrityViolationException e) {
			ChatRoom room = chatRoomRepository.findByAuctionAuctionId(auctionId)
				.orElseThrow(() -> e);
			return room.getChatRoomId();
		}
	}

	@Transactional(readOnly = true)
	public List<ChatRoomSummaryResponse> getMyRooms(UUID userId) {
		List<ChatRoom> rooms = chatRoomRepository.findMyRooms(userId);
		return rooms.stream()
			.map(room -> ChatRoomSummaryResponse.from(room, userId))
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ChatRoom getRoomOrThrow(Long roomId) {
		return chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("chat room not found"));
	}

	public void validateParticipant(ChatRoom room, UUID userId) {
		boolean isSeller = room.getSeller().getUserId().equals(userId);
		boolean isBuyer = room.getBuyer().getUserId().equals(userId);
		if (!isSeller && !isBuyer) {
			throw new IllegalStateException("no permission for this chat room");
		}
	}
}
