import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.joinu.prodigy.ProtocolRunner
import java.io.File
import java.net.InetSocketAddress


fun main(args: Array<String>) {
    runBlocking {
        if (args.isEmpty())
            println("No root validator host specified, just listening...")

        val port = 1337

        val runner = ProtocolRunner(InetSocketAddress("localhost", port))
        runner.registerProtocol(SawtoothEndorsingProtocol)

        launch {
            runner.run()
        }

        println("Listening for messages on $port")

        val rootValidatorHost = args.firstOrNull()
        if (rootValidatorHost != null) {
            println("Trying to endorse by $rootValidatorHost:$port")

            val keyFile = File("/etc/sawtooth/keys/validator.pub")
            val keyContent = keyFile.readText()
            val rootValidatorAddress = InetSocketAddress(rootValidatorHost, port)

            val result = SawtoothEndorsingProtocol.requestEndorsement(rootValidatorAddress, keyContent)

            if (result)
                println("Endorsement succeed")
            else
                println("Endorsement failed")
        }
    }
}