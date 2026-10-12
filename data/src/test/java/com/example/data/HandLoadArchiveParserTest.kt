package com.example.data.network


import com.example.data.dto.ArchiveDto
import com.example.data.dto.HandLoadDto
import com.example.domain.model.HandLoad
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class HandLoadArchiveParserTest {

    // Настройка парсера: игнорируем неизвестные ключи для устойчивости к изменениям API
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `test hand load archive json parsing`() {
        val jsonString = """
        {
            "StartDate": "10.10.2025 10:00:00",
            "EndDate": "10.10.2025 12:00:00",
            "PageNo": 1,
            "Pages": 1,
            "Records": 50,
            "Data": [
                {
                    "LoadID": 998,
                    "LoadDate": "10.10.2025 10:02:10",
                    "WPName": "АРМ оператора",
                    "Status": false,
                    "Eq": {
                        "EqID": 230,
                        "EqName": "Дозатор инертных 2",
                        "PlcName": "Контроллер БСЦ"
                    },
                    "EqCapacity": 3000,
                    "Comp": {
                        "CompID": 0,
                        "CompQty": 0.73547363
                    }
                },
                {
                    "LoadID": 999,
                    "LoadDate": "10.10.2025 10:02:10",
                    "WPName": "АРМ оператора",
                    "Status": true,
                    "Eq": {
                        "EqID": 230,
                        "EqName": "Дозатор инертных 2",
                        "PlcName": "Контроллер БСЦ"
                    },
                    "EqCapacity": 3000,
                    "Comp": {
                        "CompID": 1483,
                        "CompNo": "Г1",
                        "CompName": "Граншлак",
                        "CompQty": 1849.0396
                    }
                }
            ]
        }
        """.trimIndent()

        // Act
        val response = json.decodeFromString<ArchiveDto<HandLoadDto>>(jsonString)

        // Assert: Корневой объект
        assertEquals("10.10.2025 10:00:00", response.startDate)
        assertEquals("10.10.2025 12:00:00", response.endDate)
        assertEquals(1, response.pageNum)
        assertEquals(50, response.pages)
        assertEquals(2, response.records)

        // Assert: Первый элемент (без CompName и CompNo)
        val item1 = response.data[0]
        assertEquals(998, item1.id)
        assertEquals(false, item1.status)
        assertEquals("Дозатор инертных 2", item1.equipment.name)
        assertEquals(3000, item1.equipmentCapacity)
        assertEquals(0, item1.component.productId)
        assertEquals(0.73547363, item1.comp.compQty)
        assertNull(item1.comp.compName)  // Критически важная проверка
        assertNull(item1.comp.compNo)

        // Assert: Второй элемент (с CompName и CompNo)
        val item2 = response.data[1]
        assertEquals(999, item2.loadId)
        assertEquals(true, item2.status)
        assertEquals(1483, item2.comp.compId)
        assertEquals("Г1", item2.comp.compNo)
        assertEquals("Граншлак", item2.comp.compName)
        assertEquals(1849.0396, item2.comp.compQty)
    }
}