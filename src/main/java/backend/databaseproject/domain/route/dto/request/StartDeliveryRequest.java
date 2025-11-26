package backend.databaseproject.domain.route.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 배송 시작 요청 DTO
 * 점주가 선택한 주문 ID 리스트를 받아 배송을 시작합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "배송 시작 요청")
public class StartDeliveryRequest {

    @NotEmpty(message = "주문 ID 리스트는 필수입니다.")
    @Size(min = 1, max = 3, message = "주문은 최소 1개, 최대 3개까지 선택할 수 있습니다.")
    @Schema(description = "배송할 주문 ID 리스트 (최대 3개)", example = "[1, 2, 3]")
    private List<Long> orderIds;
}
