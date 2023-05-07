package org.itstory.gallery.controller

import mu.KotlinLogging
import org.itstory.gallery.service.MainService
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.*

@Controller
class MainController(
    @Value("\${path.base}")
    private val base: String,
    @Value("\${command.image}")
    private val command: String,
    private val service: MainService,
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/")
    fun main() = "redirect:/artist"

    @GetMapping("/artist")
    fun artist(model: Model): String {
        println("GET /artist")
        model["parent"] = "#"
        model["title"] = base
        model["list"] = service.artistsThumb()
        return "main"
    }

    @GetMapping("/artist/{a}")
    fun works(model: Model, @PathVariable a: String): String {
        val artist = service.decode(a)
        println("GET /artist/$artist")
        model["parent"] = "/artist"
        model["title"] = artist
        val path = Paths.get(base, artist)
        model["list"] = service.worksThumb(path)
        return "main"
    }

    @GetMapping("/artist/{a}/{b}")
    fun pages(model: Model, @PathVariable a: String, @PathVariable b: String): String {
        val artist = service.decode(a)
        val work = service.decode(b)
        println("GET /artist/$artist/$work")
        val path = Paths.get(base, artist, work)
        model["parent"] = "/artist/$a"
        model["title"] = "[$artist] $work"
        model["list"] = service.pagesThumb(a, b, path)
        return "main"
    }

    @GetMapping("/artist/{a}/{b}/{page}")
    fun page(model: Model, @PathVariable a: String, @PathVariable b: String, @PathVariable page: String): String {
        val artist = service.decode(a)
        val work = service.decode(b)
        model["parent"] = "/artist/$a/$b"
        model["title"] = "$artist/$work/$page"
        model["page"] = service.onePage(a, b, page)
        return "page"
    }

    @ResponseBody
    @GetMapping("/image/{a}/thumb.webp")
    fun image(@PathVariable a: String, @PathVariable img: String): ResponseEntity<Resource> {
        return try {
            val artist = service.decode(a)
            val path = Paths.get(base, artist, img)
            val resource = InputStreamResource(path.inputStream())
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"thumb.webp\"")
                .header(HttpHeaders.CONTENT_LENGTH, Files.size(path).toString())
                .body(resource)
        } catch (e: Exception) {
            logger.error { e.message }
            ResponseEntity.notFound().build()
        }
    }

    @ResponseBody
    @GetMapping("/image/{a}/{b}/{img}")
    fun image(@PathVariable a: String, @PathVariable b: String, @PathVariable img: String): ResponseEntity<Resource> {
        return try {
            val artist = service.decode(a)
            val book = service.decode(b)
            val path = Paths.get(base, artist, book, img)
            val resource = InputStreamResource(path.inputStream())
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$img\"")
                .header(HttpHeaders.CONTENT_LENGTH, Files.size(path).toString())
                .body(resource)
        } catch (e: Exception) {
            logger.error { e.message }
            ResponseEntity.notFound().build()
        }
    }

    @ResponseBody
    @GetMapping("/open/{a}/{b}/{img}")
    fun open(@PathVariable a: String, @PathVariable b: String, @PathVariable img: String) {
        val artist = service.decode(a)
        val book = service.decode(b)
        val path = Paths.get(base, artist, book, img)
        println(path)
        ProcessBuilder(command, path.pathString).start()
    }
}