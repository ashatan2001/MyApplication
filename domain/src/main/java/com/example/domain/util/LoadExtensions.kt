import com.example.domain.model.Load
import com.example.domain.model.LoadUpdateRequest
import com.example.domain.model.Terminal

/**
 * Собирает запрос на изменение из существующей заявки очереди.
 *
 * Терминал, продукт и подразделение берутся из [Load] — пользователь
 * меняет только количество. Возвращает null, если в заявке нет данных,
 * обязательных по контракту сервера.
 *
 * @param quantity Новое количество отгруженной продукции.
 * @return Запрос на изменение или null при неполных данных заявки.
 */
fun Load.toUpdateRequest(quantity: Float): LoadUpdateRequest? {
    val terminal = terminal ?: return null
    val product = product ?: return null
    val department = department ?: return null
    return LoadUpdateRequest(
        terminal = terminal,
        product = product,
        quantity = quantity,
        departmentId = department.id
    )
}

/**
 * Группирует список заявок по терминалу.
 * Заявки без указанного терминала игнорируются.
 */
fun List<Load>.groupByTerminal(): Map<Terminal, List<Load>> =
    filter { it.terminal != null }.
    groupBy { it.terminal!! }