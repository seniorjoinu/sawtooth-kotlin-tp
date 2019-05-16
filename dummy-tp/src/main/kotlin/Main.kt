import sawtooth.sdk.processor.TransactionProcessor
import kotlin.concurrent.thread


fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "You should pass the validator address in order to start this TP" }

    val tp = TransactionProcessor(args.first())
    tp.addHandler(DummyTransactionHandler())

    thread { tp.run() }.start()
}