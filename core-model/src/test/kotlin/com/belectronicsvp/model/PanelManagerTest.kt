package com.belectronicsvp.model

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PanelManagerTest {

    @Test
    fun `creates and clones panels with unique IDs`() {
        val manager = PanelManager()
        val original = manager.createPanel("Dashboard", "Telemetry")

        assertEquals("Dashboard", original.name)
        assertTrue(original.id.isNotEmpty())

        val cloned = manager.clonePanel(original, "Dashboard Clone")
        assertEquals("Dashboard Clone", cloned.name)
        assertNotEquals(original.id, cloned.id)
    }

    @Test
    fun `exports and imports panel files accurately`() {
        val manager = PanelManager()
        val tempDir = createTempDirectory("panel_tests").toFile()
        val file = File(tempDir, "rover.bep")

        val panel = manager.createPanel("Rover").copy(
            widgets = listOf(
                WidgetModel.Button(id = "b1", x = 10f, y = 10f, label = "FIRE", pressCommand = "X")
            )
        )

        val exportResult = manager.exportToFile(panel, file)
        assertTrue(exportResult.isSuccess, "Export must succeed")
        assertTrue(file.exists(), "Export file must exist")

        val importResult = manager.importFromFile(file)
        assertTrue(importResult.isSuccess, "Import must succeed")
        val importedPanel = importResult.getOrThrow()
        assertEquals("Rover", importedPanel.name)
        assertEquals(1, importedPanel.widgets.size)

        tempDir.deleteRecursively()
    }
}
