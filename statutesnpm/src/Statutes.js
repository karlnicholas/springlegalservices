import React from "react";
import queryString from "query-string";
import http from "./http-common";
import StatutesRecurse from "./StatutesRecurse";
import AppBreadcrumb from "./AppBreadcrumb";
import "./Statutes.css";

export default class Statutes extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			viewModel: null,
			path: queryString.parse(this.props.location.search).path, 
			hiddenFrag: false, 
		}
	}
	fetchStatutes(path) {
		let url = 'api';
		if ( path !== undefined ) {
			url += '?path='+path;
		}
		return http.get(url)
		.then(response => {
			this.setState({viewModel: response.data, path: path});
		});
	}
	componentDidUpdate(prevProps, prevState) {
		const parsed = queryString.parse(this.props.location.search);
		if ( parsed.path !== this.state.path) {
			this.fetchStatutes(parsed.path);
		} 
	}

	componentDidMount() {
		this.fetchStatutes(this.state.path);
	}

	render() {
		if ( this.state.viewModel !== null && this.state.viewModel.entries.length > 0 ) {
		  return (
		      <div className="container">
		      <nav className="navbar navbar-expand-lg navbar-light bg-light">
		        <a className="navbar-brand" href="/">
		          <img src="spring-logo.png" width="95" height="50" className="d-inline-block align-center" alt="" loading="lazy"/>
		        </a>
		        <div className="collapse navbar-collapse" id="navbarSupportedContent">
		        <div className="btn-group" role="group" aria-label="Button group with nested dropdown">
		        </div>
		        <form action="/" className="navbar-nav mr-auto form-inline my-2 my-lg-0" id="search-form">
		            <input type="hidden" id="hidden-path" />
		            <input type="hidden" id="hidden-term" />
		            <input type="hidden" id="hidden-frag" />
		            <input className="form-control mr-sm-2" placeholder="Search" id="search-input" aria-label="Search"/>
		            <div className="btn-group" >
		            <button className="btn btn-outline-secondary my-2 my-sm-0" id="search-submit">Submit</button>
		            <div className="btn-group" role="group">
		              <button className="btn btn-outline-secondary dropdown-toggle" data-toggle="dropdown"><span className="caret"></span></button>
		            <div className="dropdown-menu">
		             <div className="px-4 py-3">
		                <div className="form-group">
		                 <label htmlFor="inAll">All&nbsp;Of:&nbsp;&nbsp;</label>
		                  <input type="text" className="form-control" name="inAll" id="inAll" />
		                </div>
		                <div className="form-group">
		                 <label htmlFor="inNot">None&nbsp;Of:&nbsp;&nbsp;</label>
		                  <input type="text" className="form-control" name="inNot" id="inNot" />
		                </div>
		                <div className="form-group">
		                  <label htmlFor="inAny">Any&nbsp;Of:&nbsp;&nbsp;</label>
		                  <input type="text" className="form-control" name="inAny" id="inAny" />
		                </div>
		                <div className="form-group">
		                  <label htmlFor="inExact">Exact&nbsp;Phrase:&nbsp;&nbsp;</label>
		                  <input type="text" className="form-control" name="inExact" id="inExact" />
		                </div>
		                <button type="submit" className="btn btn-primary" id="search-form-input">Submit</button>
		             </div>
		            </div>
		            </div>
		            </div>
		            <button className="btn btn-light my-2 my-sm-0" name="cl" id="search-clear" >Clear</button>
		            <button className="btn my-2 my-sm-0" id="search-frag" >Fragments</button>
		            <input type="hidden" name="fs" />
		          </form>

		            <ul className="navbar-nav ml-auto">
		            <li className="nav-item dropdown">
		              <a className="nav-link dropdown-toggle" href="/" id="navbarDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Applications</a>
		              <div className="dropdown-menu" aria-labelledby="navbarDropdown">
		                <a className="dropdown-item" href="/statutes">Guided Search</a>
		                <a className="dropdown-item" href="/">Court Opinions</a>
		              </div>
		            </li>
		          </ul>
		        </div>
		      </nav>
		      <nav aria-label="breadcrumb">
		      	<ol className="breadcrumb" id="breadcrumbs">
		      		<li className='breadcrumb-item' style={{ cursor: 'pointer' }}>Home</li>
	      			<AppBreadcrumb entries={this.state.viewModel.entries} />
      			</ol>
		      </nav>
			  <StatutesRecurse entries={this.state.viewModel.entries} index={0}/>
		      </div>
		  );
		}
		return null;
	}
}