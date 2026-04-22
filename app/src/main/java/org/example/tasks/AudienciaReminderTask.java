package org.example.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.models.Conciliation;
import org.example.repositories.ConciliationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AudienciaReminderTask {

    @Autowired
    private ConciliationRepository conciliationRepository;

    @Autowired
    private static DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Scheduled(cron = "0 0 7 * * *")
    public void nextAudiencesNotify() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusHours(48);

        List<Conciliation> audiences =
                conciliationRepository.findAudiencesDuringThePeriod(now, limit);

        if (audiences.isEmpty()) {
            log.info("Nenhuma audiência agendada para as próximas 48h.");
            return;
        }

        log.warn("{} audiência(s) agendada(s) para as próximas 48h:", audiences.size());
        audiences.forEach(c -> log.warn(
                "Associado: {} | Parte contrária: {} | Data/Hora: {} | Status citação: {}",
                c.getAssociate().getName(),
                c.getOppositePartyName(),
                c.getAudienceDateTime().format(FMT),
                c.getCitationStatus()
        ));
    }

    @Scheduled(cron = "0 0 8 * * MON")
    public void weeklySummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekend = now.plusDays(7);

        List<Conciliation> audiences =
                conciliationRepository.findAudiencesDuringThePeriod(now, weekend);

        log.info("Resumo semanal: {} audiência(s) agendada(s) para os próximos 7 dias.",
                audiences.size());

        audiences.forEach(c -> log.info(
                "[{}] Associado: {} | Parte: {} | Citação: {}",
                c.getAudienceDateTime().format(FMT),
                c.getAssociate().getName(),
                c.getOppositePartyName(),
                c.getCitationStatus()
        ));
    }
}