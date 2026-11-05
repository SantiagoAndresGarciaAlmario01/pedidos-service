package com.arepa.pedidos.application.usecase;

import java.util.UUID;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import com.arepa.pedidos.domain.model.CrearPedidoRequest;
import com.arepa.pedidos.domain.model.PedidoCreadoEvent;
import com.arepa.pedidos.domain.port.in.CrearPedidoUseCase;
import com.arepa.pedidos.domain.port.out.PedidoEventPublisher;

@Service
public class CrearPedidoUseCaseImpl implements CrearPedidoUseCase {
    private final PedidoEventPublisher publisher;

    public CrearPedidoUseCaseImpl(PedidoEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public String crear(CrearPedidoRequest pedido) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        publisher.publicarPedidoCreado(new PedidoCreadoEvent(
                id, pedido.cliente(), pedido.arepas(), LocalDateTime.now().toString()));
        return id;
    }
}
