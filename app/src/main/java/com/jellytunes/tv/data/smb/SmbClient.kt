package com.jellytunes.tv.data.smb

import jcifs.CIFSContext
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbFile
import java.io.InputStream
import java.util.Properties

class SmbClient(private val config: SmbConfig) {
    private var context: CIFSContext? = null
    
    fun connect(): Boolean {
        return try {
            val props = Properties().apply {
                setProperty("jcifs.smb.client.minVersion", "SMB202")
                setProperty("jcifs.smb.client.maxVersion", "SMB311")
            }
            val baseContext = BaseContext(PropertyConfiguration(props))
            val auth = NtlmPasswordAuthenticator(
                null,
                config.username,
                config.password
            )
            context = baseContext.withCredentials(auth)
            
            // Test connection
            val root = SmbFile(config.smbUrl, context)
            root.exists()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun listFiles(path: String): List<SmbFile> {
        val ctx = context ?: return emptyList()
        return try {
            val dir = SmbFile(path, ctx)
            if (dir.isDirectory) {
                dir.listFiles()?.toList() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    fun openInputStream(path: String): InputStream? {
        val ctx = context ?: return null
        return try {
            val file = SmbFile(path, ctx)
            file.inputStream
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun fileExists(path: String): Boolean {
        val ctx = context ?: return false
        return try {
            SmbFile(path, ctx).exists()
        } catch (e: Exception) {
            false
        }
    }
    
    fun getFileSize(path: String): Long {
        val ctx = context ?: return 0
        return try {
            SmbFile(path, ctx).length()
        } catch (e: Exception) {
            0
        }
    }
    
    fun getRootUrl(): String = config.smbUrl
}
