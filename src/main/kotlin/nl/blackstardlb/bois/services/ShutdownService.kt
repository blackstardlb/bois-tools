package nl.blackstardlb.bois.services

import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import kotlin.system.exitProcess


@Service
class ShutdownService(val context: ApplicationContext) {
    fun shutDown() {
        val exitCode = SpringApplication.exit(context, ExitCodeGenerator { 0 })
        exitProcess(exitCode)
    }
}