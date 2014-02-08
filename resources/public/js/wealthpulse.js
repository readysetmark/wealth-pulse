/* @jsx React.DOM

TODO:

- Balance
    @title
    @subtitle

  - BalanceRow
      @rowclass
      @balanceclass
      @balance
      @accountstyle
      @account

*/


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



console.log("hello");

React.renderComponent(
  NavBox({}),
  document.getElementById('sidebar')
);

console.log("goodbye");
