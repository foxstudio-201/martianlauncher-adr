package com.foxstudio.martianlauncher.game.version.mod.reader

import com.foxstudio.martianlauncher.game.addons.modloader.ModLoader
import com.foxstudio.martianlauncher.game.version.mod.LocalMod
import com.foxstudio.martianlauncher.game.version.mod.ModMetadataReader
import com.foxstudio.martianlauncher.utils.file.UnpackZipException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.zip.ZipFile as JDKZipFile

object OptiFineModReader : ModMetadataReader {
    override suspend fun fromLocal(modFile: File): LocalMod = withContext(Dispatchers.IO) {
        try {
            JDKZipFile(modFile).use { zip ->
                if (zip.checkOptiFine()) {
                    LocalMod(
                        modFile = modFile,
                        fileSize = FileUtils.sizeOf(modFile),
                        id = "optifine",
                        loader = ModLoader.OPTIFINE,
                        name = "OptiFine",
                        description = null,
                        version = zip.tryGetOptiFineVersion(),
                        authors = listOf("sp614x"),
                        icon = null,
                        checkRemote = false,
                    )
                } else throw UnpackZipException("File $modFile is not a OptiFine mod.")
            }
        } catch (e: Exception) {
            if (e !is UnpackZipException) return@withContext readWithApacheZip(modFile)
            else throw e
        }
    }

    private fun readWithApacheZip(modFile: File): LocalMod {
        val zip = ZipFile.Builder()
            .setFile(modFile)
            .get()

        if (zip.checkOptiFine()) {
            return LocalMod(
                modFile = modFile,
                fileSize = FileUtils.sizeOf(modFile),
                id = "optifine",
                loader = ModLoader.OPTIFINE,
                name = "OptiFine",
                description = null,
                version = zip.tryGetOptiFineVersion(),
                authors = listOf("sp614x"),
                icon = null,
                checkRemote = false,
            )
        } else throw UnpackZipException("File $modFile is not a OptiFine mod.")
    }
}