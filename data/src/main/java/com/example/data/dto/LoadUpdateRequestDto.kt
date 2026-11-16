import com.example.data.dto.ProductDto
import com.example.data.dto.TerminalDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Тело запроса на изменение заявки в очереди терминала.
 *
 * @property terminal Блок информации о терминале.
 * @property product Блок информации о продукте.
 * @property qty Количество отгруженной продукции.
 * @property departmentId Код подразделения (строка).
 */
@Serializable
data class LoadUpdateRequestDto(
    @SerialName("TerminalInfo") val terminal: TerminalDto,
    @SerialName("ProductInfo") val product: ProductDto,
    @SerialName("qty") val qty: Float,
    @SerialName("departmentid") val departmentId: String
)