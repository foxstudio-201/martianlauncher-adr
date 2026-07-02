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

package com.foxstudio.martianlauncher.utils.device

import androidx.annotation.Keep
import com.foxstudio.martianlauncher.utils.logging.Logger
import org.apache.commons.io.FileUtils
import java.io.File

private const val TAG = "VulkanCapabilities"

@Keep
data class VulkanCapabilities(
    val apiVersionMajor: Int,
    val apiVersionMinor: Int,
    val apiVersionPatch: Int,
    val extensions: List<String>,
    val features: Map<String, Boolean>
) {
    /** Vulkan 版本字符串 */
    val versionString: String
        get() = "$apiVersionMajor.$apiVersionMinor.$apiVersionPatch"

    /** 检查 Vulkan 版本是否至少为 1.2 */
    val isVersionSupported: Boolean
        get() = apiVersionMajor > 1 || (apiVersionMajor == 1 && apiVersionMinor >= 2)

    /** 返回设备缺失的必要扩展 */
    val missingExtensions: List<String>
        get() = REQUIRED_EXTENSIONS.filter { it !in extensions }

    /** 返回设备不支持的必要功能 */
    val missingFeatures: List<String>
        get() = REQUIRED_FEATURES.filter { features[it] != true }

    /** 设备是否满足所有需求 */
    val isAllSupported: Boolean
        get() = isVersionSupported && missingExtensions.isEmpty() && missingFeatures.isEmpty()

    companion object {
        val REQUIRED_EXTENSIONS = listOf(
            "VK_KHR_dynamic_rendering",
            "VK_KHR_push_descriptor",
            "VK_KHR_synchronization2",
            "VK_EXT_vertex_attribute_divisor",
            "VK_KHR_swapchain"
        )

        val REQUIRED_FEATURES = listOf(
            "multiDrawIndirect",
            "fillModeNonSolid",
            "samplerAnisotropy",
            "shaderDrawParameters",
            "timelineSemaphore",
            "hostQueryReset",
            "synchronization2",
            "dynamicRendering",
            "vertexAttributeInstanceRateDivisor"
        )
    }
}

@Keep
fun interface VulkanLogCallback {
    fun log(level: String, message: String)
}

@Keep
object VulkanChecker {
    init {
        try {
            System.loadLibrary("vulkan_check")
            nativeSetLogCallback { level, msg ->
                when (level) {
                    "INFO" -> Logger.info(TAG, msg)
                    "WARN" -> Logger.warning(TAG, msg)
                    "ERROR" -> Logger.error(TAG, msg)
                    else -> Logger.debug(TAG, msg)
                }
            }
        } catch (e: UnsatisfiedLinkError) {
            Logger.error(TAG, "Failed to load vulkan_check library", e)
        }
    }

    /**
     * 查询系统 Vulkan 支持情况
     * @return 如果不支持 Vulkan 或初始化失败，返回 null
     */
    fun checkCapabilities(
        driverPath: String?,
        nativeDir: String?,
        cacheDir: String?
    ): VulkanCapabilities? {
        return try {
            nativeCheckVulkan(
                driverPath = driverPath,
                nativeDir = nativeDir,
                cacheDir = cacheDir,
            )?.also { caps ->
                Logger.info(TAG, "Vulkan version: ${caps.versionString}")
                Logger.info(TAG, "Version >= 1.2: ${caps.isVersionSupported}")
                if (caps.missingExtensions.isNotEmpty()) {
                    Logger.warning(TAG, "Missing required extensions: ${caps.missingExtensions}")
                }
                if (caps.missingFeatures.isNotEmpty()) {
                    Logger.warning(TAG, "Missing required features: ${caps.missingFeatures}")
                }
                Logger.info(TAG, "All requirements satisfied: ${caps.isAllSupported}")
            }
        } catch (e: UnsatisfiedLinkError) {
            Logger.error(TAG, "Native library or method not found", e)
            null
        } catch (e: Exception) {
            Logger.error(TAG, "Native check failed", e)
            null
        } finally {
            if (nativeDir != null && cacheDir != null) {
                FileUtils.deleteQuietly(File(cacheDir))
            }
        }
    }

    @Keep
    @JvmStatic
    private external fun nativeSetLogCallback(callback: VulkanLogCallback)

    @Keep
    @JvmStatic
    private external fun nativeCheckVulkan(
        driverPath: String?,
        nativeDir: String?,
        cacheDir: String?
    ): VulkanCapabilities?
}
