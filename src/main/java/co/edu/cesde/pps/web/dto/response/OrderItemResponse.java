package co.edu.cesde.pps.web.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        String image,
        Integer quantity,
        BigDecimal unitPrice,
        String unitPriceFormatted,
        BigDecimal lineTotal,
        String lineTotalFormatted
) {
}
