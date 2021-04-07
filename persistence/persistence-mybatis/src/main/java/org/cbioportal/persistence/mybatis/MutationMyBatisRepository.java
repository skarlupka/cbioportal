package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class MutationMyBatisRepository implements MutationRepository {

    @Autowired
    private MutationMapper mutationMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<Mutation> getMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                       List<Integer> entrezGeneIds, Boolean snpOnly,
                                                                       String projection, Integer pageSize,
                                                                       Integer pageNumber, String sortBy,
                                                                       String direction) {

        return mutationMapper.getMutationsBySampleListId(molecularProfileId, sampleListId, entrezGeneIds, snpOnly,
            projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public MutationMeta getMetaMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                         List<Integer> entrezGeneIds) {

        return mutationMapper.getMetaMutationsBySampleListId(molecularProfileId, sampleListId, entrezGeneIds, null);
    }

    @Override
    public List<Mutation> getMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                  List<String> sampleIds, List<Integer> entrezGeneIds, 
                                                                  String projection, Integer pageSize, 
                                                                  Integer pageNumber, String sortBy, String direction) {

        return getGroupedMolecularProfileSamples(molecularProfileIds, sampleIds)
            .entrySet()
            .stream()
            .flatMap(entry -> fetchMutationsInMolecularProfile(entry.getKey(),
                entry.getValue(),
                entrezGeneIds,
                null,
                projection,
                pageSize,
                offsetCalculator.calculate(pageSize, pageNumber),
                sortBy,
                direction).stream())
            .collect(Collectors.toList());
    }

    private Map<String,List<String>> getGroupedMolecularProfileSamples(List<String> molecularProfileIds, List<String> sampleIds) {
        Map<String,List<String>> groupMolecularProfileSamples = new HashMap<>();

        for(int i = 0; i< molecularProfileIds.size(); i++) {
            String molecularProfileId = molecularProfileIds.get(i);
            String sampleId = sampleIds.get(i);
            if(!groupMolecularProfileSamples.containsKey(molecularProfileId)) {
                List<String> sampleList = new ArrayList<>();
                sampleList.add(sampleId);
                groupMolecularProfileSamples.put(molecularProfileId,sampleList);
            } else {
                groupMolecularProfileSamples.get(molecularProfileId).add(sampleId);
            }
        }
        return groupMolecularProfileSamples;
    }

    @Override
    public MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                    List<String> sampleIds, 
                                                                    List<Integer> entrezGeneIds) {

        return mutationMapper.getMetaMutationsInMultipleMolecularProfiles(molecularProfileIds, sampleIds, entrezGeneIds,
            null);
    }

    @Override
    public List<Mutation> fetchMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                           List<Integer> entrezGeneIds, Boolean snpOnly,
                                                           String projection, Integer pageSize, Integer pageNumber,
                                                           String sortBy, String direction) {

        return mutationMapper.getMutationsBySampleIds(molecularProfileId, sampleIds, entrezGeneIds, snpOnly, projection,
            pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public MutationMeta fetchMetaMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                             List<Integer> entrezGeneIds) {

        return mutationMapper.getMetaMutationsBySampleIds(molecularProfileId, sampleIds, entrezGeneIds, null);
    }

    @Override
    public MutationCountByPosition getMutationCountByPosition(Integer entrezGeneId, Integer proteinPosStart,
                                                              Integer proteinPosEnd) {

        return mutationMapper.getMutationCountByPosition(entrezGeneId, proteinPosStart, proteinPosEnd);
    }

    // TODO: cleanup once fusion/structural data is fixed in database
    @Override
    public List<Mutation> getFusionsInMultipleMolecularProfiles(List<String> molecularProfileIds,
            List<String> sampleIds, List<Integer> entrezGeneIds, String projection, Integer pageSize,
            Integer pageNumber, String sortBy, String direction) {

        return getGroupedMolecularProfileSamples(molecularProfileIds, sampleIds)
            .entrySet()
            .stream()
            .flatMap(entry -> mutationMapper.getFusionsInMultipleMolecularProfiles(Arrays.asList(entry.getKey()),
                entry.getValue(),
                entrezGeneIds,
                null,
                projection,
                pageSize,
                offsetCalculator.calculate(pageSize, pageNumber),
                sortBy,
                direction).stream())
            .collect(Collectors.toList());
    }
    // TODO: cleanup once fusion/structural data is fixed in database

}
