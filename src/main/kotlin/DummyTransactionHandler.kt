import com.google.protobuf.ByteString
import sawtooth.sdk.processor.State
import sawtooth.sdk.processor.TransactionHandler
import sawtooth.sdk.protobuf.TpProcessRequest
import sawtooth.sdk.processor.Utils.hash512
import java.util.*
import kotlin.math.absoluteValue


class DummyTransactionHandler : TransactionHandler {
    override fun getNameSpaces() = listOf(
        hash512(transactionFamilyName().toByteArray(charset("UTF-8"))).substring(0, 6)
    )

    override fun transactionFamilyName() = "dummy"

    override fun getVersion() = "0.1"

    override fun apply(transactionRequest: TpProcessRequest?, state: State?) {
        val txnId = Random().nextLong().absoluteValue.toString()
        println("Le wild transaction-$txnId appears...")
    }
}