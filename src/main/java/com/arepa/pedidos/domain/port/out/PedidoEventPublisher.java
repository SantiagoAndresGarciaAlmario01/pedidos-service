package com.arepa.pedidos.domain.port.out;

import com.arepa.pedidos.domain.model.PedidoCreadoEvent;

public interface PedidoEventPublisher {
    void publicarPedidoCreado(PedidoCreadoEvent evento);
}
