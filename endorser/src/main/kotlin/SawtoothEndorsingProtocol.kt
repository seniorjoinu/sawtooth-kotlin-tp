import kotlinx.coroutines.sync.Mutex
import net.joinu.prodigy.AbstractProtocol
import net.joinu.prodigy.protocol
import java.io.File
import java.net.InetSocketAddress

object SEPNames {
    const val PROTOCOL_NAME = "SEP"
    const val ENDORSE = "ENDORSE"
}

object SawtoothEndorsingProtocol : AbstractProtocol() {
    val keys = mutableListOf<String>()
    private val keyMutex = Mutex()

    init {
        keys.add("/etc/sawtooth/keys/validator.pub")
    }

    private fun addKey(key: String) {
        val keyFileName = "/etc/sawtooth/keys/validator-${keys.size}.pub"
        val keyFile = File(keyFileName)
        keyFile.mkdirs()

        keyFile.writeText(key)
        keys.add(keyFileName)
    }

    override val protocol = protocol(SEPNames.PROTOCOL_NAME) {
        on(SEPNames.ENDORSE) {
            val partyPublicKey = request.getPayloadAs<String>()

            val isNodeAllowedToJoin = validate(partyPublicKey, request.sender)
            if (!isNodeAllowedToJoin) {
                request.respond(false)
                return@on
            }

            keyMutex.lock()
            addKey(partyPublicKey)

            try {
                tryToEndorse()
                keyMutex.unlock()

                request.respond(true)
            } catch (e: Throwable) {
                println(e.localizedMessage)
                keyMutex.unlock()

                request.respond(false)
            }
        }
    }

    suspend fun requestEndorsement(address: InetSocketAddress, myPublicKeyContent: String) =
        sendAndReceive<Boolean>(SEPNames.PROTOCOL_NAME, SEPNames.ENDORSE, address, myPublicKeyContent, 15000)

    private fun tryToEndorse() {
        if (keys.size < 4)
            throw RuntimeException("Unable to endorse: Not enough nodes")

        val script = createProposalScript()

        try {
            val processBuilder = ProcessBuilder("bash", script.toString())
            processBuilder.inheritIO()

            val process = processBuilder.start()
            process.waitFor()
        } finally {
            script.delete()
        }
    }

    private fun createProposalScript(): File {
        val file = File.createTempFile("script", null)
        val keysString = keys.map { File(it).readText() }.joinToString(",") { it }

        file.writeText(
            """
                #!/bin/bash
                sawset proposal create --key /etc/sawtooth/keys/validator.priv sawtooth.consensus.pbft.members=[$keysString]
            """.trimIndent()
        )

        return file
    }

    private fun validate(key: String, sender: InetSocketAddress): Boolean {
        // TODO: here we can whitelist somehow
        return true
    }
}