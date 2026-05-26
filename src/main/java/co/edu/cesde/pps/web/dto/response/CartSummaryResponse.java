package co.edu.cesde.pps.web.dto.response;

import java.math.BigDecimal;

public record CartSummaryResponse(
        Integer itemsCount,
        BigDecimal subtotal,
        String subtotalFormatted,
        BigDecimal tax,
        String taxFormatted,
        BigDecimal shipping,
        String shippingFormatted,
        BigDecimal total,
        String totalFormatted
) {
}
