package com.ecommerce.order_service.mapper;

import com.ecommerce.order_service.dto.OrderLineItemsRequest;
import com.ecommerce.order_service.dto.OrderLineItemsResponse;
import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderLineItems;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // 1. De Request a Entidad
    Order toOrder(OrderRequest orderRequest);

    // Método auxiliar (MapStruct lo usa automáticamente para convertir cada ítem de la lista)
    OrderLineItems toOrderLineItems(OrderLineItemsRequest orderLineItemsRequest);

    // 2. De Entidad a Response
    OrderResponse toOrderResponse(Order order);

    // Método auxiliar para la respuesta
    OrderLineItemsResponse toOrderLineItemsResponse(OrderLineItems orderLineItems);
}
