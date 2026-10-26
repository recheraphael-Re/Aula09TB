package br.com.cloudtech;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class LivroController {
    public record DadosLivro(@NotBlank String titulo, @NotBlank String autor,
        @NotBlank String editora, @NotNull @Min(1) @Max(9999) Integer ano,
        @NotBlank String categoria, @NotBlank String status) {}
    public record Livro(long id, String titulo, String autor, String editora,
                        int ano, String categoria, String status) {}
    private final Map<Long, Livro> livros = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong();
    @GetMapping("/")
    public Map<String, String> inicio() {
        return Map.of("mensagem", "Bem-vindo ao sistema de gerenciamento da Biblioteca CloudTech Solutions");
    }
    @GetMapping("/livros")
    public List<Livro> listar() { return livros.values().stream().sorted(Comparator.comparingLong(Livro::id)).toList(); }
    @GetMapping("/livros/{id}")
    public Livro buscar(@PathVariable long id) {
        Livro livro = livros.get(id);
        if (livro == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado");
        return livro;
    }
    @PostMapping("/livros/cadastrar")
    public ResponseEntity<Livro> cadastrar(@Valid @RequestBody DadosLivro dados) {
        long id = ids.incrementAndGet();
        Livro livro = converter(id, dados);
        livros.put(id, livro);
        return ResponseEntity.status(HttpStatus.CREATED).body(livro);
    }
    @PutMapping("/livros/alterar/{id}")
    public Livro alterar(@PathVariable long id, @Valid @RequestBody DadosLivro dados) {
        Livro atualizado = livros.computeIfPresent(id, (chave, anterior) -> converter(id, dados));
        if (atualizado == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado");
        return atualizado;
    }
    @GetMapping("/documentacao")
    public Map<String, List<String>> documentacao() {
        return Map.of("endpoints", List.of("/", "/livros", "/livros/cadastrar", "/livros/alterar/{id}", "/livros/{id}", "/documentacao"));
    }
    private Livro converter(long id, DadosLivro d) {
        return new Livro(id, d.titulo(), d.autor(), d.editora(), d.ano(), d.categoria(), d.status());
    }
}
