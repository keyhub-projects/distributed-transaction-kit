package keyhub.distributedtransactionkit.core.context.wal;

import lombok.Getter;

@Getter
public record SimpleWriteAheadLog(

) implements WriteAheadLog {

}
