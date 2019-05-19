import io.github.rybalkinsd.kohttp.dsl.httpGet
import java.io.File


fun main(args: Array<String>) {
    val error = "You should pass one of two arguments: '-k' - to submit your pubkey or '-e' - to try to endorse"

    if (args.isEmpty())
        throw RuntimeException(error)

    val arg = args.first()

    when (arg) {
        "-k" -> {
            val keysNames = args.drop(1)

            keysNames.forEach { keyName ->
                val publicKeyFile = File("/etc/sawtooth/keys/$keyName.pub")
                val privateKeyFile = File("/etc/sawtooth/keys/$keyName.priv")
                val publicKeyContent = publicKeyFile.readText()
                val privateKeyContent = privateKeyFile.readText()

                val response = httpGet {
                    host = "endorser"
                    port = 8080
                    path = "/keys/add/$keyName/$publicKeyContent/$privateKeyContent"
                }

                val result = response.body()?.string()?.toBoolean() == true

                if (result)
                    println("Your key is submitted")
                else
                    println("Your key is not submitted")
            }
        }
        "-e" -> {
            val response = httpGet {
                host = "endorser"
                port = 8080
                path = "/keys/list"
            }

            val keys = response.body()?.string()?.split(",")
                ?.map {
                    val (key, value) = it.split("=")
                    val (pub, priv) = value.split(":")
                    key to Pair(pub, priv)
                }

            if (keys.isNullOrEmpty())
                throw RuntimeException("No keys on the endorser")

            val dir = File("/etc/sawtooth/keys")
            dir.mkdirs()

            keys.forEach { (key, value) ->
                val file = dir.resolve("$key.pub")
                file.writeText(value.first)
            }
        }
        "-g" -> {
            val keyName = args.getOrNull(1) ?: throw RuntimeException("No key name specified")
            val keyFileName = args.getOrNull(2) ?: throw RuntimeException("No key file name specified")

            val response = httpGet {
                host = "endorser"
                port = 8080
                path = "/keys/get/$keyName"
            }

            val keyValue = response.body()?.string()

            if (keyValue.isNullOrEmpty())
                throw RuntimeException("Empty key response for $keyName")

            val (pub, priv) = keyValue.split(":")

            val keyDirs = File("/etc/sawtooth/keys")
            keyDirs.mkdirs()

            val pubFile = keyDirs.resolve("$keyFileName.pub")
            pubFile.writeText(pub)

            val privFile = keyDirs.resolve("$keyFileName.priv")
            privFile.writeText(priv)

            println("Files successfully written")
        }
        else -> throw RuntimeException(error)
    }
}