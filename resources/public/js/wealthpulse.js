/*****
  Sidebar Navigation
*****/

// Report
//   @className
//   @url
//   @title
var Report = React.createClass({
  render: function() {
    return React.DOM.li({className: this.props.className},
                        React.DOM.a({href: this.props.url}, this.props.title));
  }
});


// Payee
//   @className
//   @url
//   @name
//   @amountClass
//   @amount
var Payee = React.createClass({
  render: function() {
    return React.DOM.li({className: this.props.className},
                        React.DOM.a({href: this.props.url}, this.props.name, React.DOM.span({className: "pull-right " + this.props.amountClass}, this.props.amount)));
  }
});


// NavBox
var NavBox = React.createClass({
  getInitialState: function() {
    return {reports: [], payees: []};
  },
  componentWillMount: function() {
    $.ajax({
      url: 'api/nav',
      dataType: 'json',
      success: function(data) {
        this.setState(data);
      }.bind(this),
      error: function(xhr, status, err) {
        console.error("api/nav", status, err.toString());
      }.bind(this)
    });
  },
  render: function() {
    var report_nodes = [];
    var payee_nodes = [];
    var i = 0;

    for (i = 0; i < this.state.reports.length; i++) {
      var report = this.state.reports[i];
      report_nodes.push(Report(report));
    }

    for (i = 0; i < this.state.payees.length; i++) {
      var payee = this.state.payees[i];
      payee_nodes.push(Payee({className: payee.class,
                               url: payee.url,
                               name: payee.name,
                               amountClass: payee.amountClass,
                               amount: payee.amount,
                               key: payee.name}));
    }

    var div = React.DOM.div({
      className: "well",
      style: {
        padding: "8px 0"
      }
    },
    React.DOM.ul({className: "nav nav-list"},
      React.DOM.li({className: "nav-header"}, "Reports"),
      report_nodes,
      React.DOM.li({className: "nav-header"}, "Payables / Receivables"),
      payee_nodes
      )
    );
    return div;
  }
});



/*****
  Balance Report
*****/

// BalanceReportRow
//   @rowClass
//   @balanceClass
//   @balance
//   @accountStyle
//   @account
var BalanceReportRow = React.createClass({
  render: function() {
    var row = React.DOM.tr({className: this.props.rowClass},
                           [React.DOM.td({className: "currency "+ this.props.balanceClass}, this.props.balance),
                            React.DOM.td({style: this.props.accountStyle},
                                         React.DOM.a({href: "#/TODO_register_link"}, this.props.account))]);
    return row;
  }
});


// BalanceReport
//   @title
//   @subtitle
//   @balances
var BalanceReport = React.createClass({
  getInitialState: function() {
    return {title: "", subtitle: "", balances: []};
  },
  componentWillMount: function() {
    $.ajax({
      url: 'api/balance',
      dataType: 'json',
      success: function(data) {
        this.setState(data);
      }.bind(this),
      error: function(xhr, status, err) {
        console.error("api/balance", status, err.toString());
      }.bind(this)
    });
  },
  render: function() {
    var table_rows = [];
    var i = 0;

    for (i = 0; i < this.state.balances.length; i++) {
      var balance = this.state.balances[i];
      table_rows.push(BalanceReportRow(balance));
    }

    var header = React.DOM.header({className: "page-header"},
                                   React.DOM.h1(null,
                                                [this.state.title,
                                                 React.DOM.br(),
                                                 React.DOM.small(null, this.state.subtitle)]));
    var body = React.DOM.section({className: "span4"},
                                 React.DOM.table({className: "table table-hover table-condensed"},
                                                 [React.DOM.thead(null,
                                                                  React.DOM.tr(null,
                                                                               [React.DOM.th(null, "Balance"),
                                                                                React.DOM.th(null, "Account")])),
                                                  React.DOM.tbody(null, table_rows)]));

    return React.DOM.div(null, [header, body]);
  }
});



/*****
  Initialization
*****/

console.log("hello");

React.renderComponent(
  NavBox({}),
  document.getElementById('sidebar')
);

React.renderComponent(
  BalanceReport({}),
  document.getElementById('report')
);

console.log("goodbye");
