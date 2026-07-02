/*
 * Martian Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.foxstudio.martianlauncher.game.download.game.optifine

import com.foxstudio.martianlauncher.R
import com.foxstudio.martianlauncher.coroutine.Task
import com.foxstudio.martianlauncher.game.addons.mirror.MirrorSource
import com.foxstudio.martianlauncher.game.addons.mirror.SourceType
import com.foxstudio.martianlauncher.game.addons.mirror.runMirrorable
import com.foxstudio.martianlauncher.game.addons.modloader.ModLoader
import com.foxstudio.martianlauncher.game.addons.modloader.optifine.OptiFineVersion
import com.foxstudio.martianlauncher.game.addons.modloader.optifine.OptiFineVersions
import com.foxstudio.martianlauncher.setting.AllSettings
import com.foxstudio.martianlauncher.setting.enums.MirrorSourceType
import com.foxstudio.martianlauncher.utils.isChinaMainland
import com.foxstudio.martianlauncher.utils.network.downloadFileSuspend
import com.foxstudio.martianlauncher.utils.network.withSpeedReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

const val OPTIFINE_DOWNLOAD_ID = "Download.OptiFine"

fun targetTempOptiFineInstaller(tempGameDir: File, tempMinecraftDir: File, fileName: String, isNewVersion: Boolean): File {
    return if (isNewVersion) File(tempGameDir, ".temp/OptiFine.jar")
    else {
        val nameFileCleaned = fileName
            .replace("OptiFine_", "")
            .replace(".jar", "")
            .replace("preview_", "")
        val nameFileFormatted = fileName
            .replace("OptiFine_", "OptiFine-")
            .replace("preview_", "")
        File(tempMinecraftDir, "libraries/optifine/OptiFine/$nameFileCleaned/$nameFileFormatted")
    }
}

fun getOptiFineDownloadTask(
    targetTempInstaller: File,
    optifine: OptiFineVersion
): Task {
    return Task.runTask(
        id = OPTIFINE_DOWNLOAD_ID,
        dispatcher = Dispatchers.IO,
        task = { task ->
            task.updateProgress(-1f, R.string.download_game_install_optifine_fetch_download_url, optifine.realVersion)
            val optifineUrl = getOFUrlMirrorable(optifine)

            task.updateProgress(-1f, R.string.download_game_install_base_download_file, ModLoader.OPTIFINE.displayName, optifine.realVersion)
            withSpeedReport(
                onSpeedReport = { bytes ->
                    task.updateSpeed(bytes)
                },
                onClear = {
                    task.clearSpeed()
                }
            ) {
                downloadFileSuspend(
                    url = optifineUrl,
                    outputFile = targetTempInstaller
                )
            }
        }
    )
}

fun getOptiFineModsDownloadTask(
    optifine: OptiFineVersion,
    tempModsDir: File
): Task {
    return Task.runTask(
        id = OPTIFINE_DOWNLOAD_ID,
        dispatcher = Dispatchers.IO,
        task = { task ->
            task.updateProgress(-1f, R.string.download_game_install_optifine_fetch_download_url, optifine.realVersion)
            val optifineUrl = getOFUrlMirrorable(optifine)

            //开始下载为 Mod
            task.updateProgress(-1f, R.string.download_game_install_base_download_file, ModLoader.OPTIFINE.displayName, optifine.realVersion)
            withSpeedReport(
                onSpeedReport = { bytes ->
                    task.updateSpeed(bytes)
                },
                onClear = {
                    task.clearSpeed()
                }
            ) { report ->
                downloadFileSuspend(
                    url = optifineUrl,
                    outputFile = File(tempModsDir, optifine.fileName),
                    sizeCallback = report
                )
            }
        }
    )
}

private suspend fun getOFUrlMirrorable(
    optifine: OptiFineVersion
): String {
    return if (isChinaMainland()) {
        runMirrorable(
            when (AllSettings.fileDownloadSource.getValue()) {
                MirrorSourceType.OFFICIAL_FIRST -> listOf(
                    fetchOptiFineDownloadUrl(optifine, 5),
                    getDownloadUrlWithBMCLAPI(optifine, 5 + 30)
                )
                MirrorSourceType.MIRROR_FIRST -> listOf(
                    getDownloadUrlWithBMCLAPI(optifine, 30),
                    fetchOptiFineDownloadUrl(optifine, 30 + 60)
                )
            }
        )!!
    } else {
        fetchOptiFineDownloadUrl(optifine)
    }
}

/**
 * 从官方源获取 OptiFine 主文件下载链接
 */
private fun fetchOptiFineDownloadUrl(
    optifine: OptiFineVersion,
    delayMillis: Long
): MirrorSource<String> = MirrorSource(
    delayMillis = delayMillis,
    type = SourceType.OFFICIAL
) {
    fetchOptiFineDownloadUrl(optifine)
}

private suspend fun fetchOptiFineDownloadUrl(
    optifine: OptiFineVersion
) = withContext(Dispatchers.IO) {
    OptiFineVersions.fetchOptiFineDownloadUrl(optifine.fileName) ?: throw CantFetchingOptiFineUrlException()
}

/**
 * 从镜像源获取 OptiFine 主文件下载链接
 */
private fun getDownloadUrlWithBMCLAPI(
    optifine: OptiFineVersion,
    delayMillis: Long
): MirrorSource<String> = MirrorSource(
    delayMillis = delayMillis,
    type = SourceType.BMCLAPI
) {
    val inherit = if (optifine.inherit == "1.8" || optifine.inherit == "1.9") "${optifine.inherit}.0" else optifine.inherit
    val displayNameStripped = optifine.displayName.removePrefix("${optifine.inherit} ")

    val suffix = if (optifine.isPreview) {
        "HD_U_${displayNameStripped.replace(" ", "/")}"
    } else {
        "HD_U/$displayNameStripped"
    }

    "https://bmclapi2.bangbang93.com/optifine/$inherit/$suffix"
}