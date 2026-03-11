### Ramas (Branches)

No hay restricciones de integrantes para las ramas (puede haber 1 o 4 a la vez).
  - `main`: Solo código estable y probado (entregas de Sprint).
  - `integration`: Donde se van añadiendo e integrando las funcionalidades que desarrollan en ramas separadas.
  - `feature/nombre-tarea`: Para cada funcionalidad nueva.
  

### Como trabajar con git de forma ordenada

1. Crea una nueva rama desde `integration` para desarrollar de forma aisla tu funcionalidad.
2. Haz los cambios que requieras en tu rama.
3. Cada cambio que hagas, subelo a tu rama del repositorio (push) mediante un commit (descripcion breve).
4. Cuando hayas terminado de implementar tu funcionalidad, haz un **Pull Request** hacia `integration` para que otro lo revise.
5. Si todo funciona bien y no requiere cambios, se hace **merge** con la rama `integration`, si requiere cambios se hace review de los fallos.
6. La rama `integration` contiene la funcionalidad desarrollada en la rama aislada.
