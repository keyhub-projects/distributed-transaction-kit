package keyhub.distributedtransactionkit.core.etc;

import java.time.Instant;
import java.util.UUID;

public class UuidV7Generator {
	public static UUID generate() {
		long unixTimeMillis = Instant.now().toEpochMilli();
		long mostSigBits = (unixTimeMillis << 16);
		mostSigBits |= (0x7L << 12);
		long leastSigBits = (long) (Math.random() * Long.MAX_VALUE);
		leastSigBits &= ~(0xC000000000000000L);
		leastSigBits |= 0x8000000000000000L;
		return new UUID(mostSigBits, leastSigBits);
	}
}