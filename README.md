﻿
# Práctica a realizar (Opcional)

En esta práctica será una primera toma de contacto con la creación de agentes y una comunicación básica entre ellos para alcanzar los objetivos que tiene cada agente.

  

La práctica consiste en realizar una simulación con agentes sobre el funcionamiento de un restaurante.

  

## Definición de Constantes

Para la realización de la práctica hay que definir los enumerados con los elementos que se describen:

  

-  **OrdenComanda**

-  *ENTRANTE*, *PRINCIPAL*, *POSTRE*

  

-  **Plato**

-  *NOMBRE_DEL_PLATO(OrdenComanda,precio)*

- La cantidad de platos estará a cargo de cada uno de vosotros cuando desarrolléis el proyecto.

  

## Formato del fichero de configuración

Para las pruebas de la práctica se tendrá que aportar un fichero de configuración con el siguiente formato:

  

- Estará compuesto por líneas donde estará el nombre del agente, clase y en la línea siguiente los parámetros de configuración para ese agente. Los parámetros estarán en la descripción del agente.

-  `nombreAgente:es.ujaen.ssmma.agentes.ClaseAgente`

-  `parámetros agente`

  

-  **AgenteMonitor** :

Será el agente que se crea para la prueba de la práctica y su parámetro es el fichero de configuración.

Procesará el fichero de configuración y creará todos los agentes presentes con sus parámetros.

  

## Agentes para la práctica

Las tareas de los agentes serán diseñadas por cada uno de los alumnos. Los agentes tienen unos parámetros y unos objetivos que deben alcanzar antes de finalizar.

  

Todos los agentes:

- Deben ser robustos y dar soluciones a las posibles incidencias en la ejecución.

- Deberán mostrar de forma clara su ejecución y se guardarán en un archivo el resultado de la ejecución de los agentes.

  

-  **AgenteRestaurante**

- Los parámetros del agente será la capacidad de usuarios que puede atender hasta finalizar su servicio. También tendrá definido el número de servicios que podrá dar antes de finalizar.

- Los objetivos del agente son los siguientes:

- Atender la petición de entrada que haga un cliente, si hay disponibilidad de espacio deberá atenderlo.

- El servicio a los clientes estará ordenado según el orden de la comanda.

- La preparación del plato será encargado a una cocina que tenga disponibilidad para ese plato.

  

-  **AgenteCocina**

- Los parámetros del agente será la cantidad de cada plato que podrá preparar antes de finalizar.

- Los objetivos del agente son los siguientes:

- Atender solicitudes de los restaurantes para preparar un plato dependiendo de la disponibilidad.

- Indicar cuando el plato ya ha sido preparado.

  

-  **AgenteCliente**

- Los parámetros del agente será una lista de servicios que quiere completar. Cada servicio tendrá un plato para el orden de la comanda.

- Los objetivos del agente son los siguientes:

- Encontrar el restaurante para un servicio, cuando finalice un servicio se entiende que el cliente ha abandonado el restaurante.

## Análisis

Se describirán las estructuras de datos, variables compartidas y procedimientos necesarios para comprender el diseño que se realiza de la práctica.

### Constantes

- aleatorio = Random
- TIPO_SERVICIO = "SERVICIO"
- PRIMERO = 0
- MIN_TIEMPO_COCINADO = 4000
- MAX_TIEMPO_COCINADO = 7000
- MIN_TIEMPO_PEDIR_PLATOS=10000
- MAX_TIEMPO_PEDIR_PLATOS=16000
- MAXIMOS_INTENTOS_COMER=3
- D100=101
- ARCHIVO_GUARDADO = "resultado.txt"
  

### Definición de los protocolos de comunicación
Para la realización de la práctica se hará uso del protocolo Contract Net y por ende, del protocolo Propose que este lleva implícito para la comunicación entre el Cliente y Restaurante.
Entre el Restaurante y Cocina se hará únicamente uso del protocolo Propose.

El uso del protocolo Contract Net se justifica por los siguientes motivos:

 1. Contract Net permite que los agentes pueden colaborar en la resolución de tareas complejas que requieren la cooperación de varios participantes.
 2. Permite la negociación entre los agentes: La negociación es un aspecto importante en esta práctica en decisiones y el protocolo Contract Net proporciona un marco adecuado para la negociación y la toma de decisiones como por ejemplo, si el cliente puede entrar al restaurante
 3. Permite la asignación dinámica de tareas: Con este protocolo, las tareas pueden ser asignadas dinámicamente a diferentes agentes, lo que permite una mayor flexibilidad en la asignación de tareas.
 4. Permite la gestión de recursos compartidos: El protocolo Contract Net permite la gestión de recursos compartidos entre los diferentes agentes, lo que facilita la colaboración en la resolución de tareas complejas.

El uso del protocolo Propose entre el Restaurante y la Cocina se debe a:

 1. El protocolo Propose se utiliza cuando un agente necesita solicitar una propuesta a otro agente, pero no necesita realizar una negociación en profundidad como la que se realiza en el protocolo Contract Net. En este caso, el agente emisor solicita una propuesta y el agente receptor envía una respuesta con la propuesta. El emisor evalúa la propuesta y toma una decisión.
 2. El protocolo Propose es adecuado cuando se necesita una respuesta rápida y simple, y no se requiere una negociación compleja para llegar a un acuerdo. En este caso, la cocina únicamente responderá al Restaurante si es capaz o no de preparar el plato que este le solicitud, atendiendo a la disponibilidad de ese tipo
 
 ### Agente Cliente
El agente Cliente será el encargado de solicitar los platos en servicios (Conjunto de platos conformados por entrante, principal y postre). El agente Cliente únicamente establecerá comunicación directa con el Restaurante, de forma que este será el intermediario que le suministrará los Platos. El cliente solicitará servicios mientras que disponga de dinero suficiente

Las principales tareas que debe implementar este agente son las siguientes:

*  **TareaSubscripciónDF**: Se debe registrar en el servicio de páginas amarillas para localizar los diferentes agentes Restaurante y así solicitar entrar en ellos.


*  **TareaSolicitarServicio**: El agente Cliente solicitará un servicio al agente Restaurante. Este último le responderá con el precio que costaría cocinarlo para que el cliente confirme su cocinado para finalmente, comerse el servicio. Implementa el protocolo Contract Net.

Esta tarea se desarrolla de la siguiente manera:

En primera instancia, el cliente inicializa una acción de negociación (Cfp) en la que se solicita al restaurante (participante) una oferta para un determinado servicio del cliente.
Esta CFP contendrá por tanto implícitamente una solicitud de entrar al restaurante y la precondición será que este disponga de aforo suficiente

La respuesta de este podrá ser un 
	- `Inform-done`: En caso de no entender la solicitud del cliente
	- `Refuse`: En caso de que el restaurante no disponga de aforo suficiente
	- `Propose`: En caso de que sí lo disponga. Esta acción propose se encargará de proponer al cliente el cocinar un plato.

El cliente en función del dinero disponible y del coste propuesto por el restaurante podrá ejercer las siguientes acciones:
- `accept-proposal`: Aceptar la propuesta del Restaurante
- `Reject-proposal`: Rechazar la propuesta del Restaurante

Finalmente, el restaurante responderá con un inform-done, inform-ref o failure.


-   `inform-done`: Este mensaje se utiliza para notificar que la tarea ha sido realizada exitosamente. Contiene una lista de los resultados, en este caso, el servicio o el conjunto de platos para que se los coma el Cliente.
    
-   `inform-ref`: Este mensaje se utiliza para notificar que la tarea no se puede realizar. Contiene una explicación del motivo por el cual la tarea no puede ser completada, por ejemplo, en caso de no existir ninguna Cocina disponible.

-  `failure`: Este mensaje puede ser enviado por el Restaurante que ha fallado en cumplir con una tarea asignada o en responder a una solicitud de propuesta.

En resumen, el mensaje `inform-done` se utiliza para notificar resultados exitosos, mientras que el mensaje `inform-ref` se utiliza para notificar resultados no exitosos. El mensaje `failure` notifica que ha habido algún error

A continuación se muestra el diagrama de secuencia que satisface las restricciones previamente descritas:


```mermaid
sequenceDiagram
	participant Cliente
	participant Restaurante 
	loop Completa todos los servicios de comida 
		Cliente->>Restaurante: CFP (servicioSolicitado)
		Note right of Cliente: Evalúa la solicitud del servivio 
		
		alt Sin aforo
			Restaurante-->>Cliente: Refuse (Justificacion)
			Note right of Restaurante: No hay aforo e impide entrar al Cliente 
		else no entendido
			Restaurante-->>Cliente: Not-understood (Justificacion)
		else propuesta de precio
			Restaurante-->>Cliente: Propose (costo,platosDisponibles) 
			Note right of Restaurante: Propone un precio para el servicio
		end
		
		alt precio aceptado
			Cliente->>Restaurante: accept-proposal (platosDisponibles,costo)
			Note right of Cliente: El cliente dispone de dinero suficiente
		else
			Cliente->>Restaurante: reject-proposal (platosDisponibles, costo, motivo)
		end
		
		alt platos entregados
			Restaurante-->>Cliente: inform-done (platosDisponibles)
			Note right of Restaurante: Devuelve la lista de platos del cliente, estos han sido cocinados
		else
			Restaurante-->>Cliente: inform-ref (motivo)
		else fallo
			Restaurante-->>Cliente: failure (motivo)
		end
	end

```

### Agente Restaurante
El agente Cliente será el encargado de proponer a la cocina cocinar los platos que le ha pedido el cliente. El agente Restaurante será el único capaz de cominicarse con la Cocina. 

*  **TareaSubscripciónDF**: Se debe registrar en el servicio de páginas amarillas para localizar los diferentes agentes Cocina para encontrar un agente que sea capaz de cocinar el tipo de plato indicado por el Cliente. También deberá localizar a los Clientes para entregarles los platos que solicitaron


*  **TareaSolicitarCocinado**: El agente Restaurante propondrá que los servicios sean cocinados por un agente Cocina. Este último aceptará la propuesta siempre y cuando tenga disponibilidad para cada tipo de plato. Implementa el protocolo Propose.


Esta tarea se desarrolla de la siguiente manera:

Primero, el Restaurante propone a la cocina cocinar un plato perteneciente a la lista de platos que le encargó previamente el cliente.

La cocina podrá realizar las siguientes acciones en respuesta del Restaurante:
- `accept-proposal`: Aceptar la propuesta del Restaurante
- `Reject-proposal`: Rechazar la propuesta del Restaurante

A continuación se muestra el diagrama de secuencia que satisface las restricciones previamente descritas:


```mermaid
sequenceDiagram
	participant Restaurante
	participant Cocina

		Restaurante->>Cocina: Propose (Plato)
		Note right of Restaurante: Evalúa la solicitud del plato 
		
		alt Con disponibilidad
			Cocina-->>Restaurante: Accept-proposal(Plato)
			Note right of Restaurante: Hay disponibilidad del plato y procede a cocinarlo
		else
			Cocina-->>Restaurante: Reject-proposal (Plato)
		end

```



## Diseño

### Datos
Tipos de datos necesarios para la solución de la práctica:

#### Agente Cliente
Variables locales:
-  `resultado: Resultado`
-  `listaAgentes: ArrayList<AID>[]`
- `restAenviar:  entero`
- `dineroRestante:  flotante`

#### Agente Restaurante
-  `platosPedidos: ArrayList<Plato>`
- `resultado: Resultado`
-  `platosCocinados: ArrayList<Plato>`
- `capacidadComensales:  entero`
- `numComensales:  entero`
- `numComensales:  entero`
- `numServicios:  entero`
-   `listaAgentes: ArrayList<AID>[]`

#### Agente Cocina
-  `capacidadPlatos: entero`
- `resultado: Resultado`
-  `tiposOrdenComanda: Set<OrdenComanda>`
- `comandasDisponiblesPorOrdenComanda:  Map<String, Integer>`
-   `listaAgentes: ArrayList<AID>[]`

### Clases de apoyo
#### Resultado
Clase encargada de almacenar los datos del resultado de la ejecución del Restaurante. Almacenará varios datos que se almacenarán en un fichero de salida

### Diseño de los Agentes

#### Agente Cliente

- **TareaSolicitarServicioIniciador**

	Esta tarea corresponde al Agente Cliente para resolver el rol iniciador del protocolo **FIPA-Contract-		Net** y para ello debemos personalizar la clase `ContractNetInitiator`

	Esta tarea se compondrá de las siguientes partes:
	-  
	```

	```

