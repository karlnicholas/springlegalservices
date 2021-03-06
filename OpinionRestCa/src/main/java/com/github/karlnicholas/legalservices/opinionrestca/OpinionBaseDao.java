package com.github.karlnicholas.legalservices.opinionrestca;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.github.karlnicholas.legalservices.opinion.model.*;
import com.github.karlnicholas.legalservices.sqlutil.ResultSetIterable;
import com.github.karlnicholas.legalservices.statute.StatuteKey;

public class OpinionBaseDao {
	private final DataSource dataSource;
	
	public OpinionBaseDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * return a List of OpinionBases from keys
	 * @param opinionKeys reference keys
	 * @return List of {@link OpinionBase} getOpinionsWithStatuteCitations
	 * @throws SQLException if database error
	 */
	public List<OpinionBase> getOpinionsWithStatuteCitations(List<OpinionKey> opinionKeys) throws SQLException {
		if ( opinionKeys.size() == 0 )
			return Collections.emptyList();
		StringBuilder sb = new StringBuilder("select ooc.id as ooc_id, " + 
				"ooc.countreferringopinions as ooc_countreferringopinions, " + 
				"ooc.opiniondate as ooc_opiniondate, " + 
				"ooc.volume as ooc_volume, " + 
				"ooc.vset as ooc_vset, " + 
				"ooc.page as ooc_page, " + 
				"ooc.title as ooc_title, " + 
				"osc.countreferences as osc_countreferences,  " + 
				"sc.designated as sc_designated, " + 
				"sc.lawcode as sc_lawcode, " + 
				"sc.sectionnumber  as sc_sectionnumber " + 
				"from opinionbase ooc " + 
				"left outer join opinionstatutecitation osc on ooc.id = osc.opinionbase_id  " + 
				"left outer join statutecitation sc on osc.statutecitation_id = sc.id " + 
				"where (ooc.volume, ooc.vset, ooc.page) in ");
		sb.append("(");
		for ( int i=0; i < opinionKeys.size(); ++i ) {
			sb.append("(?,?,?),");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(")");
		try (Connection con = dataSource.getConnection();
			 PreparedStatement ps = con.prepareStatement(sb.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		) {
			for ( int i=0; i < opinionKeys.size(); ++i ) {
				ps.setInt(i*3+1, opinionKeys.get(i).getVolume());
				ps.setInt(i*3+2, opinionKeys.get(i).getVset());
				ps.setInt(i*3+3, opinionKeys.get(i).getPage());
			}
			try (ResultSet rs = ps.executeQuery()) {
				return new ResultSetIterable<OpinionBase>(rs, rs2 -> mapOpinionsWithStatuteCitations(rs2)).stream()
						.collect(Collectors.groupingBy(OpinionBase::getId, Collectors.reducing((ob1, ob2)->{
							ob1.getStatuteCitations().addAll(ob2.getStatuteCitations());
							return ob1;
						}))).values().stream().map(Optional::get).collect(Collectors.toList());
			}
			
		}
	}

	private OpinionBase mapOpinionsWithStatuteCitations(ResultSet resultSet) throws SQLException {
		OpinionBase opinionBase = new OpinionBase(
				resultSet.getInt("ooc_volume"), 
				resultSet.getInt("ooc_vset"), 
				resultSet.getInt("ooc_page"));
		opinionBase.setId(resultSet.getInt("ooc_id"));
		opinionBase.setCountReferringOpinions(resultSet.getInt("ooc_countreferringopinions"));
		if ( resultSet.getObject("ooc_opiniondate") != null ) opinionBase.setOpinionDate(((Date)resultSet.getObject("ooc_opiniondate")).toLocalDate());
		opinionBase.setTitle(resultSet.getString("ooc_title"));
		opinionBase.setStatuteCitations(new HashSet<>());
		if ( resultSet.getString("sc_lawcode") != null &&  resultSet.getString("sc_sectionnumber") != null ) {
			StatuteCitation sc = new StatuteCitation(new StatuteKey(resultSet.getString("sc_lawcode"), resultSet.getString("sc_sectionnumber")));
			sc.setDesignated(resultSet.getBoolean("sc_designated"));
			OpinionStatuteCitation osc = new OpinionStatuteCitation(sc, opinionBase, resultSet.getInt("osc_countreferences"));
			opinionBase.getStatuteCitations().add(osc);
		}
		return opinionBase;
	}

	public String callSlipOpinionUpdateNeeded() throws SQLException {
		try (Connection con = dataSource.getConnection();
			PreparedStatement ps = con.prepareCall("{call checkSlipOpinionUpdate()}");
		) {
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getString(1);
			}
		}
	}
	public void updateSlipOpinionList(String string) throws SQLException {
		try (Connection con = dataSource.getConnection();
			 PreparedStatement ps = con.prepareStatement("update slipopinionlist set slipopinionlist=? where id = 1" );
		) {
			ps.setString(1, string);
			ps.executeUpdate();
		}
	}

}
