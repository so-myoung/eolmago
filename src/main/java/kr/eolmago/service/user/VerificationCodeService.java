package kr.eolmago.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeService {

    private static final String PREFIX = "sms:verification:";
    private static final int EXPIRATION_MINUTES = 5;

    private final StringRedisTemplate redisTemplate;
    private final SmsService smsService;

    public void generateAndSendVerificationCode(String phoneNumber) {
        String verificationCode = generateRandomCode();
        log.debug("인증 코드 생성: phoneNumber={}, code={}", phoneNumber, verificationCode);

        redisTemplate.opsForValue().set(
                PREFIX + phoneNumber,
                verificationCode,
                EXPIRATION_MINUTES,
                TimeUnit.MINUTES
        );

        smsService.sendVerificationCode(phoneNumber, verificationCode);
    }

    public boolean verifyCode(String phoneNumber, String code) {
        String storedCode = redisTemplate.opsForValue().get(PREFIX + phoneNumber);
        boolean isValid = code.equals(storedCode);
        log.debug("인증 코드 검증: phoneNumber={}, code={}, storedCode={}, isValid={}",
                phoneNumber, code, storedCode, isValid);
        return isValid;
    }

    public void deleteVerificationCode(String phoneNumber) {
        log.debug("인증 코드 삭제: phoneNumber={}", phoneNumber);
        redisTemplate.delete(PREFIX + phoneNumber);
    }

    private String generateRandomCode() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000); // 6자리 숫자
        return String.valueOf(number);
    }
}
