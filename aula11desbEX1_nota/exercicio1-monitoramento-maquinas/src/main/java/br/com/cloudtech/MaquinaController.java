package br.com.cloudtech;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MaquinaController {
    public record Cadastro(@NotBlank String nome, @NotBlank String tipo,
                           @NotBlank String setor, @NotBlank String status) {}
    public record Maquina(long id, String nome, String tipo, String setor, String status) {}
    public record DadosProducao(@NotNull @Positive Long maquinaId,
        @NotNull @DecimalMin("0") BigDecimal consumoKw,
        @NotNull LocalTime horarioAcionamento, @NotNull @PositiveOrZero Integer pecasProduzidas) {}
    public record Producao(long id, long maquinaId, BigDecimal consumoKw,
        LocalTime horarioAcionamento, int pecasProduzidas, LocalDateTime registradoEm) {}
    private final Map<Long, Maquina> maquinas = new ConcurrentHashMap<>();
    private final List<Producao> historico = new CopyOnWriteArrayList<>();
    private final AtomicLong ids = new AtomicLong();
    private final AtomicLong registros = new AtomicLong();

    @GetMapping("/")
    public Map<String, String> inicio() { return Map.of("mensagem", "Bem-vindo ao monitoramento de máquinas da CloudTech Solutions"); }
    @GetMapping("/status")
    public Map<String, String> status() { return Map.of("status", "online", "mensagem", "Aplicação de monitoramento em execução"); }
    @GetMapping("/versao")
    public Map<String, String> versao() { return Map.of("versao", "1.0.0"); }
    @GetMapping("/maquinas")
    public List<Maquina> listar() { return maquinas.values().stream().sorted(Comparator.comparingLong(Maquina::id)).toList(); }
    @PostMapping("/maquinas/cadastro")
    public ResponseEntity<Maquina> cadastrar(@Valid @RequestBody Cadastro dados) {
        long id = ids.incrementAndGet();
        Maquina maquina = new Maquina(id, dados.nome(), dados.tipo(), dados.setor(), dados.status());
        maquinas.put(id, maquina);
        return ResponseEntity.status(HttpStatus.CREATED).body(maquina);
    }
    @PostMapping("/producao")
    public ResponseEntity<Producao> registrar(@Valid @RequestBody DadosProducao dados) {
        verificarMaquina(dados.maquinaId());
        Producao producao = new Producao(registros.incrementAndGet(), dados.maquinaId(), dados.consumoKw(),
            dados.horarioAcionamento(), dados.pecasProduzidas(), LocalDateTime.now());
        historico.add(producao);
        return ResponseEntity.status(HttpStatus.CREATED).body(producao);
    }
    @GetMapping("/producao")
    public List<Producao> consultar(@RequestParam(required = false) Long maquinaId) {
        if (maquinaId != null) verificarMaquina(maquinaId);
        return historico.stream().filter(p -> maquinaId == null || p.maquinaId() == maquinaId).toList();
    }
    private void verificarMaquina(long id) {
        if (!maquinas.containsKey(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Máquina não encontrada");
    }
}
