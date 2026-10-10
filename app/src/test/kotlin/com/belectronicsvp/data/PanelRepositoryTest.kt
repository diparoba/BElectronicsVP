package com.belectronicsvp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PanelRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var repository: PanelRepository

    @Before
    fun setUp() {
        val dir = tempFolder.newFolder("test_panels")
        repository = PanelRepository(baseDir = dir)
    }

    @Test
    fun testInitialSeededPanels() {
        val panels = repository.getAllPanels()
        assertTrue(panels.isNotEmpty())
        assertTrue(panels.any { it.name == "ESP32 Sensor Telemetry" })
        assertTrue(panels.any { it.name == "Robot Controller" })
    }

    @Test
    fun testCreateAndGetPanel() {
        val created = repository.createPanel("My Custom IoT", "Testing panel")
        assertEquals("My Custom IoT", created.name)
        assertEquals("Testing panel", created.description)

        val retrieved = repository.getPanel(created.id)
        assertNotNull(retrieved)
        assertEquals(created.id, retrieved?.id)
    }

    @Test
    fun testRenamePanel() {
        val created = repository.createPanel("Original Name")
        val updated = repository.renamePanel(created.id, "Updated Name")
        assertNotNull(updated)
        assertEquals("Updated Name", updated?.name)

        val retrieved = repository.getPanel(created.id)
        assertEquals("Updated Name", retrieved?.name)
    }

    @Test
    fun testClonePanel() {
        val created = repository.createPanel("Base Panel")
        val cloned = repository.clonePanel(created.id, "Cloned Panel")
        assertNotNull(cloned)
        assertEquals("Cloned Panel", cloned?.name)
        assertTrue(cloned?.id != created.id)

        val all = repository.getAllPanels()
        assertTrue(all.any { it.id == created.id })
        assertTrue(all.any { it.id == cloned?.id })
    }

    @Test
    fun testDeletePanel() {
        val created = repository.createPanel("To Delete")
        val deleted = repository.deletePanel(created.id)
        assertTrue(deleted)

        val retrieved = repository.getPanel(created.id)
        assertNull(retrieved)
    }

    @Test
    fun testExportAndImportJson() {
        val created = repository.createPanel("Export Test")
        val jsonResult = repository.exportToJson(created.id)
        assertTrue(jsonResult.isSuccess)
        val json = jsonResult.getOrThrow()
        assertTrue(json.contains("Export Test"))

        val importResult = repository.importFromJson(json)
        assertTrue(importResult.isSuccess)
        val imported = importResult.getOrThrow()
        assertEquals("Export Test", imported.name)
        assertTrue(imported.id != created.id)
    }

    @Test
    fun testCreatePanelWithOrientation() {
        val created = repository.createPanel(
            name = "Vertical Panel",
            description = "Portrait panel test",
            orientation = com.belectronicsvp.model.PanelOrientation.PORTRAIT,
            baseDensity = 10
        )
        assertEquals(com.belectronicsvp.model.PanelOrientation.PORTRAIT, created.orientation)
        assertEquals(10, created.baseDensity)

        val retrieved = repository.getPanel(created.id)
        assertNotNull(retrieved)
        assertEquals(com.belectronicsvp.model.PanelOrientation.PORTRAIT, retrieved?.orientation)
        assertEquals(10, retrieved?.baseDensity)
    }

    @Test
    fun testSavePanelWithWidgetsAndResize() {
        val created = repository.createPanel("Widget Canvas Test")
        val buttonWidget = com.belectronicsvp.model.WidgetModel.Button(
            id = "btn_1",
            x = 2f,
            y = 3f,
            width = 3f,
            height = 2f,
            label = "START",
            pressCommand = "GO",
            releaseCommand = "STOP"
        )
        val sliderWidget = com.belectronicsvp.model.WidgetModel.Slider(
            id = "sld_1",
            x = 0f,
            y = 0f,
            width = 6f,
            height = 2f,
            label = "Speed",
            commandPrefix = "V: ",
            commandSuffix = "\n",
            minValue = 0,
            maxValue = 9
        )

        val updatedPanel = created.copy(
            widgets = listOf(buttonWidget, sliderWidget),
            baseDensity = 12
        )
        repository.savePanel(updatedPanel)

        val reloaded = repository.getPanel(created.id)
        assertNotNull(reloaded)
        assertEquals(2, reloaded?.widgets?.size)
        assertEquals(12, reloaded?.baseDensity)

        val reloadedBtn = reloaded?.widgets?.find { it.id == "btn_1" } as com.belectronicsvp.model.WidgetModel.Button
        assertEquals("GO", reloadedBtn.pressCommand)
        assertEquals("STOP", reloadedBtn.releaseCommand)

        // Test resizing widget
        val resizedWidgets = reloaded.widgets.map { w ->
            if (w.id == "btn_1") (w as com.belectronicsvp.model.WidgetModel.Button).copy(width = 5f, height = 3f) else w
        }
        repository.savePanel(reloaded.copy(widgets = resizedWidgets))

        val afterResize = repository.getPanel(created.id)
        val resizedBtn = afterResize?.widgets?.find { it.id == "btn_1" } as com.belectronicsvp.model.WidgetModel.Button
        assertEquals(5f, resizedBtn.width)
        assertEquals(3f, resizedBtn.height)
    }
}
