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
        this.setState({reports: data.reports,
                       payees: data.payees});
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
      report_nodes.push(Report({url: report.url,
                                title: report.title,
                                key: report.title}));
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
  render: function() {
    var table_rows = [];
    var i = 0;

    for (i = 0; i < this.props.balances.length; i++) {
      var balance = this.props.balances[i];
      table_rows.push(BalanceReportRow(balance));
    }

    var header = React.DOM.header({className: "page-header"},
                                   React.DOM.h1(null,
                                                [this.props.title,
                                                 React.DOM.br(),
                                                 React.DOM.small(null, this.props.subtitle)]));
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
  BalanceReport({"title":"Balance Sheet","subtitle":"As of today","balances":[{"key":"Assets","account":"Assets","balance":"$401,616.66","balanceClass":"assets","accountStyle":{"padding-left":"8px;"}},{"key":"Assets:Home:Brixton","account":"Home:Brixton","balance":"$350,000.00","balanceClass":"assets","accountStyle":{"padding-left":"28px;"}},{"key":"Assets:Investments:BookValue","account":"Investments:BookValue","balance":"$39,603.32","balanceClass":"assets","accountStyle":{"padding-left":"28px;"}},{"key":"Assets:Investments:BookValue:RRSP:TD","account":"RRSP:TD","balance":"$39,091.58","balanceClass":"assets","accountStyle":{"padding-left":"48px;"}},{"key":"Assets:Investments:BookValue:RRSP:TD:CanadianBondIndex","account":"CanadianBondIndex","balance":"$10,236.12","balanceClass":"assets","accountStyle":{"padding-left":"68px;"}},{"key":"Assets:Investments:BookValue:RRSP:TD:CanadianEquityIndex","account":"CanadianEquityIndex","balance":"$10,513.57","balanceClass":"assets","accountStyle":{"padding-left":"68px;"}},{"key":"Assets:Investments:BookValue:RRSP:TD:InternationalEquityIndex","account":"InternationalEquityIndex","balance":"$9,673.65","balanceClass":"assets","accountStyle":{"padding-left":"68px;"}},{"key":"Assets:Investments:BookValue:RRSP:TD:USEquityIndex","account":"USEquityIndex","balance":"$8,668.24","balanceClass":"assets","accountStyle":{"padding-left":"68px;"}},{"key":"Assets:Investments:BookValue:Regular:Royal:CanadianMoneyMarket","account":"Regular:Royal:CanadianMoneyMarket","balance":"$511.74","balanceClass":"assets","accountStyle":{"padding-left":"48px;"}},{"key":"Assets:Receivables:Analee","account":"Receivables:Analee","balance":"$180.00","balanceClass":"assets","accountStyle":{"padding-left":"28px;"}},{"key":"Assets:Savings","account":"Savings","balance":"$11,833.34","balanceClass":"assets","accountStyle":{"padding-left":"28px;"}},{"key":"Assets:Savings:ING","account":"ING","balance":"$5,005.88","balanceClass":"assets","accountStyle":{"padding-left":"48px;"}},{"key":"Assets:Savings:INGTaxFree","account":"INGTaxFree","balance":"$5,327.46","balanceClass":"assets","accountStyle":{"padding-left":"48px;"}},{"key":"Assets:Savings:Royal","account":"Royal","balance":"$1,500.00","balanceClass":"assets","accountStyle":{"padding-left":"48px;"}},{"key":"Liabilities","account":"Liabilities","balance":"($275,381.44)","balanceClass":"liabilities","accountStyle":{"padding-left":"8px;"}},{"key":"Liabilities:Credit:Visa","account":"Credit:Visa","balance":"($151.38)","balanceClass":"liabilities","accountStyle":{"padding-left":"28px;"}},{"key":"Liabilities:Mortgage:Brixton","account":"Mortgage:Brixton","balance":"($275,215.31)","balanceClass":"liabilities","accountStyle":{"padding-left":"28px;"}},{"key":"Liabilities:Payables:MomDad","account":"Payables:MomDad","balance":"($14.75)","balanceClass":"liabilities","accountStyle":{"padding-left":"28px;"}},{"key":"Total","account":"","balance":"$126,235.22","rowClass":"grand_total"}]}),
  document.getElementById('report')
);

console.log("goodbye");
