package opca.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import opca.model.OpinionBase;
import opca.model.OpinionKey;
import opca.model.OpinionStatuteCitation;
import opca.model.StatuteCitation;
import opca.model.StatuteKey;

@Service
public class OpinionBaseDao {
	private final JdbcTemplate jdbcTemplate;

	public OpinionBaseDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
/*
	public OpinionBase findOpinionByKeyFetchReferringOpinions(OpinionKey key) {
		return jdbcTemplate.queryForStream((conn)->{
			PreparedStatement ps = conn.prepareStatement("select " + 
					"oboc.id as oboc_id, " + 
					"oboc.countreferringopinions as oboc_countreferringopinions, " + 
					"oboc.opiniondate as oboc_opiniondate, " + 
					"oboc.page oboc_page, " + 
					"oboc.volume oboc_volume, " + 
					"oboc.vset oboc_vset, " + 
					"oboc.title oboc_title, " +
					"obro.id as obro_id, " + 
					"obro.countreferringopinions as obro_countreferringopinions, " + 
					"obro.opiniondate as obro_opiniondate, " + 
					"obro.page obro_page, " + 
					"obro.volume obro_volume, " + 
					"obro.vset obro_vset, " + 
					"obro.title obro_title" + 
					"from opinionbase oboc " + 
					"left outer join opinionbase_opinioncitations obroj on oboc.id = obroj.referringopinions_id  " + 
					"left outer join opinionbase obro on obroj.opinioncitations_id = obro.id " + 
					"where (oboc.page, oboc.volume, oboc.vset) = (?,?,?)" );
				ps.setInt(0, key.getPage());
				ps.setInt(1, key.getVolume());
				ps.setInt(2, key.getVset());
			return ps;
		}, this::mapOpinionsWithReferringOpinions).collect(Collectors.groupingBy(OpinionBase::getId, Collectors.reducing((ob1, ob2)->{
			ob1.getReferringOpinions().addAll(ob2.getReferringOpinions());
			return ob1;
		}))).values().iterator().next().get();
	}
*/
	/**
	 * select distinct o from OpinionBase o 
	 * left join fetch o.referringOpinions 
	 * where o.opinionKey in :opinionKeys"
	 * 
	 * @param opinionKeys
	 * @return
	 */
	
	public List<OpinionBase> opinionsWithReferringOpinions(List<OpinionKey> opinionKeys) {
		return jdbcTemplate.queryForStream((conn)->{
			StringBuilder sb = new StringBuilder( "select " + 
					"oboc.id as oboc_id, " + 
					"oboc.countreferringopinions as oboc_countreferringopinions, " + 
					"oboc.opiniondate as oboc_opiniondate, " + 
					"oboc.page oboc_page, " + 
					"oboc.volume oboc_volume, " + 
					"oboc.vset oboc_vset, " + 
					"oboc.title oboc_title, " +
					"obro.id as obro_id, " + 
					"obro.countreferringopinions as obro_countreferringopinions, " + 
					"obro.opiniondate as obro_opiniondate, " + 
					"obro.page obro_page, " + 
					"obro.volume obro_volume, " + 
					"obro.vset obro_vset, " + 
					"obro.title obro_title" + 
					"from opinionbase oboc " + 
					"left outer join opinionbase_opinioncitations obroj on oboc.id = obroj.referringopinions_id  " + 
					"left outer join opinionbase obro on obroj.opinioncitations_id = obro.id " + 
					"where (oboc.page, oboc.volume, oboc.vset) in " );
			sb.append("(");
			for ( int i=0; i < opinionKeys.size(); ++i ) {
				sb.append("(?,?,?),");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append(")");
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			for ( int i=0; i < opinionKeys.size(); ++i ) {
				ps.setInt(i*3, opinionKeys.get(i).getPage());
				ps.setInt(i*3+1, opinionKeys.get(i).getVolume());
				ps.setInt(i*3+2, opinionKeys.get(i).getVset());
			}
			return ps;
		}, this::mapOpinionsWithReferringOpinions).collect(Collectors.groupingBy(OpinionBase::getId, Collectors.reducing((ob1, ob2)->{
			ob1.getReferringOpinions().addAll(ob2.getReferringOpinions());
			return ob1;
		}))).values().stream().map(Optional::get).collect(Collectors.toList());
	}

	/**
	 *  
	 *  select distinct o from OpinionBase o 
	 *  left join fetch o.opinionCitations ooc 
	 *  left join fetch ooc.statuteCitations oocsc 
	 *  left join fetch oocsc.statuteCitation 
	 *  where o.id in :opinionIds"
	 *   
	 * @param opinionIds
	 * @return
	 */
	public List<OpinionBase> fetchOpinionCitationsForOpinions(List<Integer> opinionIds) {
		return jdbcTemplate.queryForStream((conn)->{
			StringBuilder sb = new StringBuilder("select o.id as o_id, " + 
					"o.countreferringopinions as o_countreferringopinions, " + 
					"o.opiniondate as o_opiniondate, " + 
					"o.page as o_page, " + 
					"o.volume as o_volume, " + 
					"o.vset as o_vset, " + 
					"o.title as o_title, " + 
					"sc.id as sc_id,  " + 
					"ooc.id as ooc_id, " + 
					"ooc.countreferringopinions as ooc_countreferringopinions, " + 
					"ooc.opiniondate as ooc_opiniondate, " + 
					"ooc.page as ooc_page, " + 
					"ooc.volume as ooc_volume, " + 
					"ooc.vset as ooc_vset, " + 
					"ooc.title as ooc_title, " + 
					"osc.countreferences as osc_countreferences, " +  
					"sc.designated as sc_designated, " + 
					"sc.lawcode as sc_lawcode, " + 
					"sc.sectionnumber  as sc_sectionnumber" + 
					"from opinionbase o " + 
					"left outer join opinionbase_opinioncitations oocj on o.id = oocj.opinioncitations_id  " + 
					"left outer join opinionbase ooc on oocj.referringopinions_id = ooc.id " + 
					"left outer join opinionstatutecitation osc on ooc.id = osc.opinionbase_id  " + 
					"left outer join statutecitation sc on osc.statutecitation_id = sc.id "
					+ "where o.id in ");
			sb.append("(");
			for ( int i=0; i < opinionIds.size(); ++i ) {
				sb.append("?,");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append(")");
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			for ( int i=0; i < opinionIds.size(); ++i ) {
				ps.setInt(i, opinionIds.get(i));
			}
			return ps;
		}, this::mapFetchOpinionCitationsForOpinions).collect(Collectors.groupingBy(OpinionBase::getId, Collectors.reducing((ob1, ob2)->{
			ob1.getOpinionCitations().addAll(ob2.getOpinionCitations());
			return ob1;
		}))).values().stream().map(Optional::get).collect(Collectors.toList());
	}

	private OpinionBase mapFetchOpinionCitationsForOpinions(ResultSet resultSet, int rowNum) throws SQLException {
		OpinionBase opinionBase = new OpinionBase(
				resultSet.getInt("o_page"), 
				resultSet.getInt("o_volume"), 
				resultSet.getInt("o_vset"));
		opinionBase.setId(resultSet.getInt("o_id"));
		opinionBase.setCountReferringOpinions(resultSet.getInt("o_countreferrringopinions"));
		if ( resultSet.getRef("o_opiniondate") != null ) opinionBase.setOpinionDate((LocalDate)resultSet.getObject("o_opiniondate"));
		opinionBase.setTitle(resultSet.getString("o_title"));
		opinionBase.setOpinionCitations(new HashSet<>());
		OpinionBase opinionBaseCitation = new OpinionBase(
				resultSet.getInt("ooc_page"), 
				resultSet.getInt("ooc_volume"), 
				resultSet.getInt("ooc_vset"));
		opinionBaseCitation.setId(Integer.valueOf(resultSet.getString("ooc_id")));
		opinionBaseCitation.setCountReferringOpinions(resultSet.getInt("ooc_countreferrringopinions"));
		if ( resultSet.getRef("ooc_opiniondate") != null ) opinionBaseCitation.setOpinionDate((LocalDate)resultSet.getObject("ooc_opiniondate"));
		opinionBaseCitation.setTitle(resultSet.getString("ooc_title"));
		opinionBase.getOpinionCitations().add(opinionBaseCitation);
		StatuteCitation sc = new StatuteCitation(new StatuteKey(resultSet.getString("sc_lawcode"), resultSet.getString("sc_lsectionnumber")));
		OpinionStatuteCitation osc = new OpinionStatuteCitation(sc, opinionBaseCitation, resultSet.getInt("osc_countreferences"));
		opinionBaseCitation.setStatuteCitations(new HashSet<>());
		opinionBaseCitation.getStatuteCitations().add(osc);
		return opinionBase;
	}
	/**
	 * query="select distinct oro from OpinionBase o2 
	 * left outer join o2.opinionCitations oro 
	 * left join fetch oro.referringOpinions 
	 * where o2.id in :opinionIds"),
	 * 
	 * @param opinionIds
	 * @return
	 */
	public List<OpinionBase> fetchCitedOpinionsWithReferringOpinions(List<Integer> opinionIds) {
		return jdbcTemplate.queryForStream((conn)->{
			StringBuilder sb = new StringBuilder("select " + 
					"oboc.id as oboc_id, " + 
					"oboc.countreferringopinions as oboc_countreferringopinions, " + 
					"oboc.opiniondate as oboc_opiniondate, " + 
					"oboc.page oboc_page, " + 
					"oboc.volume oboc_volume, " + 
					"oboc.vset oboc_vset, " + 
					"oboc.title oboc_title, " + 
					"obro.id as obro_id, " + 
					"obro.countreferringopinions as obro_countreferringopinions, " + 
					"obro.opiniondate as obro_opiniondate, " + 
					"obro.page obro_page, " + 
					"obro.volume obro_volume, " + 
					"obro.vset obro_vset, " + 
					"obro.title obro_title" + 
					"from opinionbase ob " + 
					"left outer join opinionbase_opinioncitations obocj on ob.id = obocj.opinioncitations_id  " + 
					"left outer join opinionbase oboc on obocj.referringopinions_id = oboc.id " + 
					"left outer join opinionbase_opinioncitations obocroj on oboc.id = obocroj.referringopinions_id " + 
					"left outer join opinionbase obro on obocroj.opinioncitations_id = obro.id " + 
					"where ob.id in");
			sb.append("(");
			for ( int i=0; i < opinionIds.size(); ++i ) {
				sb.append("?,");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append(")");
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			for ( int i=0; i < opinionIds.size(); ++i ) {
				ps.setInt(i, opinionIds.get(i));
			}
			return ps;
		}, this::mapOpinionsWithReferringOpinions).collect(Collectors.groupingBy(OpinionBase::getId, Collectors.reducing((ob1, ob2)->{
			ob1.getReferringOpinions().addAll(ob2.getReferringOpinions());
			return ob1;
		}))).values().stream().map(Optional::get).collect(Collectors.toList());
	}
	
	private OpinionBase mapOpinionsWithReferringOpinions(ResultSet resultSet, int rowNum) throws SQLException {
		OpinionBase opinionBase = new OpinionBase(
				resultSet.getInt("oboc_page"), 
				resultSet.getInt("oboc_volume"), 
				resultSet.getInt("oboc_vset"));
		opinionBase.setId(resultSet.getInt("oboc_id"));
		opinionBase.setCountReferringOpinions(resultSet.getInt("oboc_countreferrringopinions"));
		if ( resultSet.getRef("oboc_opiniondate") != null ) opinionBase.setOpinionDate((LocalDate)resultSet.getObject("oboc_opiniondate"));
		opinionBase.setTitle(resultSet.getString("oboc_title"));
		opinionBase.setReferringOpinions(new HashSet<>());
		OpinionBase opinionBaseReferring = new OpinionBase(
				resultSet.getInt("obro_page"), 
				resultSet.getInt("obro_volume"), 
				resultSet.getInt("obro_vset"));
		opinionBaseReferring.setId(Integer.valueOf(resultSet.getString("obro_id")));
		opinionBaseReferring.setCountReferringOpinions(resultSet.getInt("obro_countreferrringopinions"));
		if ( resultSet.getRef("obro_opiniondate") != null ) opinionBaseReferring.setOpinionDate((LocalDate)resultSet.getObject("obro_opiniondate"));
		opinionBaseReferring.setTitle(resultSet.getString("obor_title"));
		opinionBase.getReferringOpinions().add(opinionBaseReferring);
		return opinionBase;
	}
	
	/**
	 * create table opinionbase 
	 * (dtype integer not null, 
	 * id integer not null auto_increment, 
	 * countreferringopinions integer not null, 
	 * opiniondate date, 
	 * page integer not null, 
	 * volume integer not null, 
	 * vset integer not null, 
	 * title varchar(127), 
	 * primary key (id)) engine=InnoDB;
	 * 
	 * @param opinion
	 */
	public void insert(OpinionBase opinion) {
		jdbcTemplate.update((conn)->{
			PreparedStatement ps = conn.prepareStatement(
					"insert into opinionbase(dtype, countreferringopinions, opiniondate, page, volume, vset, title) " +
					"values(?, ?, ?, ?, ?, ?, ?)");
			ps.setInt(0, opinion.getDtype().getDtype());
			ps.setInt(1, opinion.getCountReferringOpinions());
			ps.setObject(2, opinion.getOpinionDate());
			ps.setInt(3, opinion.getOpinionKey().getPage());
			ps.setInt(4, opinion.getOpinionKey().getVolume());
			ps.setInt(5, opinion.getOpinionKey().getVset());
			ps.setString(6, opinion.getTitle());
			return ps;
		});
	}

	public void update(OpinionBase opinion) {
		jdbcTemplate.update((conn)->{
			PreparedStatement ps = conn.prepareStatement(
					"update opinionbase set countreferringopinions = ?, opiniondate=?, title = ?");
			ps.setInt(1, opinion.getCountReferringOpinions());
			ps.setObject(2, opinion.getOpinionDate());
			ps.setString(6, opinion.getTitle());
			return ps;
		});
	}
}
