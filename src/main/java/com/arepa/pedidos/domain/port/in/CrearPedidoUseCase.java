package com.arepa.pedidos.domain.port.in;

import com.arepa.pedidos.domain.model.CrearPedidoRequest;

public interface CrearPedidoUseCase {
    String crear(CrearPedidoRequest pedido);
}
