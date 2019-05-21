import com.google.protobuf.ByteString
import sawtooth.sdk.processor.State
import sawtooth.sdk.processor.TransactionHandler
import sawtooth.sdk.processor.Utils.hash512
import sawtooth.sdk.protobuf.TpProcessRequest
import java.util.*
import kotlin.math.absoluteValue


class DummyTransactionHandler : TransactionHandler {
    override fun getNameSpaces() = listOf(
        hash512(transactionFamilyName().toByteArray(charset("UTF-8"))).substring(0, 6)
    )

    override fun transactionFamilyName() = "dummy"

    override fun getVersion(): String {
        val version = System.getenv("SAWTOOTH_VERSION")

        return if (version.isEmpty()) "1.0"
        else version
    }

    override fun apply(transactionRequest: TpProcessRequest?, state: State?) {
        val txnId = Random().nextLong().absoluteValue.toString()
        println("Le wild transaction-$txnId appears...")

        val addr = "1cf1266e282c41be5e4254d8820772c5518a2c5a8c0c7f7eda19594a7eb539453e1ed7"

        val prevState = state?.getState(listOf(addr))

        if (prevState == null)
            println("State is null for some reason...")
        else {
            val prevValue = prevState.values.first().toByteArray()
            val newValue = hash512(prevValue)
            state.setState(
                mapOf(addr to ByteString.copyFromUtf8(newValue)).entries
            )
            println("State is changed successfully")
        }
    }
}