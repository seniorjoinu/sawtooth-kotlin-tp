import com.google.protobuf.ByteString
import io.github.rybalkinsd.kohttp.dsl.httpPost
import sawtooth.sdk.protobuf.*
import sawtooth.sdk.signing.Secp256k1Context
import sawtooth.sdk.signing.Secp256k1PrivateKey
import sawtooth.sdk.signing.Signer


// submits empty transaction
fun main(args: Array<String>) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")

    println("Welcome to dummy-tp transaction submitter")

    val endpoint = args.firstOrNull()?.trim()

    if (endpoint.isNullOrEmpty())
        throw RuntimeException("Specify an endpoint to submit txn to (e.g. rest-api-0:8008)")

    submitEmptyTxn(endpoint)
}

fun submitEmptyTxn(restApiEndpoint: String) {
    val context = Secp256k1Context()
    val privateKey = Secp256k1PrivateKey.fromHex("80378f103c7f1ea5856d50f2dcdf38b97da5986e9b32297be2de3c8444c38c08")
    val signer = Signer(context, privateKey)

    val payloadBytes = ByteArray(0)

    val header = TransactionHeader.newBuilder()
        .setFamilyName("dummy")
        .setFamilyVersion("0.1")
        .addInputs("1cf1266e282c41be5e4254d8820772c5518a2c5a8c0c7f7eda19594a7eb539453e1ed7")
        .addOutputs("1cf1266e282c41be5e4254d8820772c5518a2c5a8c0c7f7eda19594a7eb539453e1ed7")
        .setSignerPublicKey(signer.publicKey.hex())
        .setBatcherPublicKey(signer.publicKey.hex())
        .setPayloadSha512("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e")
        .build()

    val headerSignature = signer.sign(header.toByteArray())
    val txn = Transaction.newBuilder()
        .setHeader(header.toByteString())
        .setHeaderSignature(headerSignature)
        .setPayload(ByteString.copyFrom(payloadBytes))
        .build()

    val batchHeader = BatchHeader.newBuilder()
        .setSignerPublicKey(signer.publicKey.hex())
        .addAllTransactionIds(listOf(txn.headerSignature))
        .build()

    val batchHeaderSignature = signer.sign(batchHeader.toByteArray())

    val batch = Batch.newBuilder()
        .setHeader(batchHeader.toByteString())
        .setHeaderSignature(batchHeaderSignature)
        .addTransactions(txn)
        .build()

    val batchList = BatchList.newBuilder()
        .addBatches(batch)
        .build()

    val (apiHost, apiPort) = restApiEndpoint.split(":")

    val response = httpPost {
        host = apiHost
        port = apiPort.toInt()
        path = "/batches"

        body("application/octet-stream") {
            bytes(batchList.toByteArray())
        }
    }

    if (response.isSuccessful)
        println("Successfully submitted the txn!")
    else
        println(response.message())
}